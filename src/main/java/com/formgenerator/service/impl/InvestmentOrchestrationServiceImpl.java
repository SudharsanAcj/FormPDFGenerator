package com.formgenerator.service.impl;

import com.formgenerator.audit.AuditService;
import com.formgenerator.config.AadhaarESignProperties;
import com.formgenerator.domain.dto.request.InvestmentRequest;
import com.formgenerator.domain.dto.response.InvestmentResponse;
import com.formgenerator.domain.dto.response.TransactionStatusResponse;
import com.formgenerator.domain.mapper.InvestorDetailsMapper;
import com.formgenerator.domain.model.InvestorDetails;
import com.formgenerator.domain.model.enums.AmcName;
import com.formgenerator.esign.model.ESignRequest;
import com.formgenerator.esign.model.ESignResponse;
import com.formgenerator.esign.model.ESignStatus;
import com.formgenerator.esign.pkcs7.Pkcs7SignatureEmbedder;
import com.formgenerator.exception.PdfTemplateNotFoundException;
import com.formgenerator.mapping.AmcFieldMapping;
import com.formgenerator.mapping.AmcMappingRegistry;
import com.formgenerator.service.InvestmentOrchestrationService;
import com.formgenerator.service.esign.AadhaarESignService;
import com.formgenerator.service.rti.RtiSubmissionService;
import com.formgenerator.store.TransactionRecord;
import com.formgenerator.store.TransactionStatus;
import com.formgenerator.store.TransactionStore;
import com.formgenerator.strategy.FormFillerStrategy;
import com.formgenerator.strategy.FormFillerStrategyFactory;
import com.formgenerator.util.PdfHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentOrchestrationServiceImpl implements InvestmentOrchestrationService {

    private final InvestorDetailsMapper mapper;
    private final FormFillerStrategyFactory strategyFactory;
    private final AmcMappingRegistry mappingRegistry;
    private final AadhaarESignService eSignService;
    private final RtiSubmissionService rtiSubmissionService;
    private final Pkcs7SignatureEmbedder pkcs7Embedder;
    private final TransactionStore transactionStore;
    private final AuditService auditService;
    private final ResourceLoader resourceLoader;
    private final AadhaarESignProperties eSignProperties;

    @Override
    public InvestmentResponse initiateFreshPurchase(InvestmentRequest request) {
        String transactionId = UUID.randomUUID().toString();
        log.info("Initiating fresh purchase — txn: {}, AMC: {}", transactionId, request.getAmcName());

        // 1. Map DTO → domain model
        InvestorDetails investor = mapper.toInvestorDetails(request);

        // 2. Persist initial transaction record
        TransactionRecord record = TransactionRecord.builder()
                .transactionId(transactionId)
                .status(TransactionStatus.INITIATED)
                .investorDetails(investor)
                .createdAt(Instant.now())
                .build();
        transactionStore.save(record);

        // 3. Load AMC field mapping and PDF template
        AmcName amcName = investor.getAmcName();
        AmcFieldMapping fieldMapping = mappingRegistry.getMapping(amcName);
        byte[] pdfTemplateBytes = loadPdfTemplate(fieldMapping.getPdfTemplatePath(), amcName);

        // 4. Fill PDF form using AMC strategy
        FormFillerStrategy strategy = strategyFactory.getStrategy(amcName);
        byte[] filledPdfBytes = strategy.fill(pdfTemplateBytes, investor, fieldMapping);

        record.setFilledPdfBytes(filledPdfBytes);
        record.setStatus(TransactionStatus.PDF_FILLED);
        transactionStore.save(record);

        auditService.record(auditService.buildEvent(transactionId, "PDF_FILLED", amcName, "SUCCESS",
                "Form filled for investor PAN: " + maskPan(investor.getPan())));

        // 5. Build and initiate eSign request
        String documentHash = PdfHashUtil.sha256Base64(filledPdfBytes);
        String docInfo = amcName.getDisplayName() + " Fresh Purchase - " +
                investor.getFirstName() + " " + investor.getLastName();

        ESignRequest eSignRequest = ESignRequest.builder()
                .transactionId(transactionId)
                .aspId(eSignProperties.getAsp().getId())
                .authMode(eSignProperties.getAsp().getAuthMode())
                .version(eSignProperties.getGateway().getVersion())
                .responseUrl(eSignProperties.getGateway().getResponseUrl())
                .documentHash(documentHash)
                .docInfo(docInfo)
                .timestamp(DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                .build();

        record.setStatus(TransactionStatus.ESIGN_PENDING);
        transactionStore.save(record);

        String eSignRedirectUrl = eSignService.initiateESign(eSignRequest);

        auditService.record(auditService.buildEvent(transactionId, "ESIGN_INITIATED", amcName, "PENDING",
                "eSign request dispatched to gateway"));

        return InvestmentResponse.builder()
                .transactionId(transactionId)
                .status(TransactionStatus.ESIGN_PENDING.name())
                .eSignRedirectUrl(eSignRedirectUrl)
                .amcName(amcName.getCode())
                .timestamp(Instant.now())
                .message("Please complete Aadhaar eSign to proceed")
                .build();
    }

    @Override
    public void completeAfterESign(String transactionId, ESignResponse eSignResponse) {
        log.info("Completing transaction after eSign — txn: {}, status: {}",
                transactionId, eSignResponse.getStatus());

        TransactionRecord record = transactionStore.get(transactionId);
        InvestorDetails investor = record.getInvestorDetails();
        AmcName amcName = investor.getAmcName();

        if (eSignResponse.getStatus() != ESignStatus.SUCCESS) {
            record.setStatus(TransactionStatus.ESIGN_FAILED);
            record.setESignErrorCode(eSignResponse.getErrorCode());
            transactionStore.save(record);
            auditService.record(auditService.buildEvent(transactionId, "ESIGN_FAILED", amcName,
                    "FAILED", "errCode=" + eSignResponse.getErrorCode()));
            return;
        }

        // 6. Embed PKCS#7 signature into PDF
        String signerName = investor.getFirstName() + " " + investor.getLastName();
        byte[] signedPdfBytes = pkcs7Embedder.embedSignature(
                record.getFilledPdfBytes(),
                eSignResponse.getPkcs7SignatureBase64(),
                signerName);

        record.setSignedPdfBytes(signedPdfBytes);
        record.setStatus(TransactionStatus.ESIGN_COMPLETED);
        transactionStore.save(record);

        auditService.record(auditService.buildEvent(transactionId, "ESIGN_COMPLETED", amcName,
                "SUCCESS", "Signature embedded successfully"));

        // 7. Submit to RTI
        try {
            String rtiAck = rtiSubmissionService.submitSignedForm(signedPdfBytes, investor, amcName);
            record.setRtiAcknowledgementNumber(rtiAck);
            record.setStatus(TransactionStatus.COMPLETED);
            transactionStore.save(record);

            auditService.record(auditService.buildEvent(transactionId, "RTI_SUBMITTED", amcName,
                    "SUCCESS", "RTI ack: " + rtiAck));

        } catch (Exception e) {
            log.error("RTI submission failed for txn: {} — {}", transactionId, e.getMessage());
            record.setStatus(TransactionStatus.RTI_FAILED);
            record.setRtiErrorMessage(e.getMessage());
            transactionStore.save(record);

            auditService.record(auditService.buildEvent(transactionId, "RTI_FAILED", amcName,
                    "FAILED", e.getMessage()));
        }
    }

    @Override
    public TransactionStatusResponse getTransactionStatus(String transactionId) {
        TransactionRecord record = transactionStore.get(transactionId);
        InvestorDetails investor = record.getInvestorDetails();
        return TransactionStatusResponse.builder()
                .transactionId(transactionId)
                .status(record.getStatus().name())
                .amcName(investor.getAmcName().getCode())
                .rtiAcknowledgementNumber(record.getRtiAcknowledgementNumber())
                .eSignErrorCode(record.getESignErrorCode())
                .rtiErrorMessage(record.getRtiErrorMessage())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    private byte[] loadPdfTemplate(String templatePath, AmcName amcName) {
        try {
            return resourceLoader.getResource(templatePath).getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new PdfTemplateNotFoundException(amcName.getCode(), templatePath);
        }
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 4) return "****";
        return "****" + pan.substring(pan.length() - 4);
    }
}

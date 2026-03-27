package com.formgenerator.api.v1;

import com.formgenerator.domain.dto.request.InvestmentRequest;
import com.formgenerator.domain.dto.response.InvestmentResponse;
import com.formgenerator.domain.dto.response.TransactionStatusResponse;
import com.formgenerator.domain.mapper.InvestorDetailsMapper;
import com.formgenerator.domain.model.InvestorDetails;
import com.formgenerator.exception.PdfTemplateNotFoundException;
import com.formgenerator.mapping.AmcFieldMapping;
import com.formgenerator.mapping.AmcMappingRegistry;
import com.formgenerator.service.InvestmentOrchestrationService;
import com.formgenerator.service.esign.AadhaarESignService;
import com.formgenerator.store.TransactionRecord;
import com.formgenerator.store.TransactionStore;
import com.formgenerator.strategy.FormFillerStrategyFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Dev-only endpoints for previewing filled PDFs without eSign or RTI.
 * Only active under the "dev" Spring profile.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
@Profile("dev")
public class DevController {

    private final InvestorDetailsMapper mapper;
    private final AmcMappingRegistry mappingRegistry;
    private final FormFillerStrategyFactory strategyFactory;
    private final ResourceLoader resourceLoader;
    private final InvestmentOrchestrationService orchestrationService;
    private final AadhaarESignService eSignService;
    private final TransactionStore transactionStore;

    /**
     * Fill the AMC form and return it as a downloadable PDF immediately —
     * no eSign, no RTI. For visual verification during development.
     */
    @PostMapping(value = "/preview-filled-form", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> previewFilledForm(
            @Valid @RequestBody InvestmentRequest request) throws IOException {

        log.info("[DEV] Generating filled PDF preview for AMC: {}", request.getAmcName());

        InvestorDetails investor = mapper.toInvestorDetails(request);
        AmcFieldMapping mapping = mappingRegistry.getMapping(investor.getAmcName());
        byte[] template = loadTemplate(mapping, investor);
        byte[] filled = strategyFactory.getStrategy(investor.getAmcName())
                .fill(template, investor, mapping);

        String filename = "filled_" + investor.getAmcName().getCode()
                + "_" + investor.getPan() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(filled.length)
                .body(filled);
    }

    /**
     * Simulates the mock eSign gateway callback.
     * Call this after /api/v1/investments/fresh-purchase to complete the full flow.
     */
    @GetMapping("/mock-esign-callback")
    public ResponseEntity<TransactionStatusResponse> mockESignCallback(
            @RequestParam String txn) {

        log.warn("[MOCK-ESIGN] Auto-completing eSign for txn: {}", txn);
        // Trigger the callback using the mock eSign service — passes the txn as the "response XML"
        var eSignResponse = eSignService.processCallback(txn);
        orchestrationService.completeAfterESign(txn, eSignResponse);
        return ResponseEntity.ok(orchestrationService.getTransactionStatus(txn));
    }

    /**
     * Download the signed PDF after the full flow completes.
     */
    @GetMapping(value = "/download/{transactionId}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadSignedPdf(@PathVariable String transactionId) {
        TransactionRecord record = transactionStore.get(transactionId);
        byte[] pdfBytes = record.getSignedPdfBytes() != null
                ? record.getSignedPdfBytes() : record.getFilledPdfBytes();

        String filename = "form_" + transactionId + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }

    private byte[] loadTemplate(AmcFieldMapping mapping, InvestorDetails investor) throws IOException {
        try {
            return resourceLoader.getResource(mapping.getPdfTemplatePath())
                    .getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new PdfTemplateNotFoundException(
                    investor.getAmcName().getCode(), mapping.getPdfTemplatePath());
        }
    }
}

package com.formgenerator.service.esign.impl;

import com.formgenerator.esign.model.ESignRequest;
import com.formgenerator.esign.model.ESignResponse;
import com.formgenerator.esign.model.ESignStatus;
import com.formgenerator.service.esign.AadhaarESignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Base64;

/**
 * Mock eSign service for dev/test profile.
 * Returns a stub redirect URL and a fake PKCS#7 signature so the full
 * orchestration pipeline can be exercised without CDAC credentials.
 *
 * Only active under the "dev" Spring profile (@Primary overrides the real impl).
 */
@Slf4j
@Service
@Primary
@Profile("dev")
public class MockAadhaarESignServiceImpl implements AadhaarESignService {

    // Minimal DER-encoded self-signed cert stub (not cryptographically valid —
    // sufficient to exercise the PDF embedding path without a real HSM).
    private static final String MOCK_PKCS7_BASE64 =
            Base64.getEncoder().encodeToString("MOCK_PKCS7_SIGNATURE".getBytes());
    private static final String MOCK_CERT_BASE64 =
            Base64.getEncoder().encodeToString("MOCK_X509_CERTIFICATE".getBytes());

    @Override
    public String initiateESign(ESignRequest request) {
        log.warn("[MOCK-ESIGN] Returning mock eSign redirect URL for txn: {}", request.getTransactionId());
        return "http://localhost:8080/form-pdf-generator/api/v1/dev/mock-esign-callback?txn="
                + request.getTransactionId();
    }

    @Override
    public ESignResponse processCallback(String responseXml) {
        // For mock: the txn id is embedded in the XML or as a simple string
        log.warn("[MOCK-ESIGN] Processing mock eSign callback");
        return ESignResponse.builder()
                .status(ESignStatus.SUCCESS)
                .transactionId(extractTxn(responseXml))
                .pkcs7SignatureBase64(MOCK_PKCS7_BASE64)
                .userCertificateBase64(MOCK_CERT_BASE64)
                .responseTimestamp(Instant.now().toString())
                .build();
    }

    private String extractTxn(String input) {
        // Simple: input may just be the transaction id in mock flow
        return input != null ? input.trim() : "UNKNOWN";
    }
}

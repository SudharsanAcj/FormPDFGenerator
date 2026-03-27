package com.formgenerator.service;

import com.formgenerator.domain.dto.request.InvestmentRequest;
import com.formgenerator.domain.dto.response.InvestmentResponse;
import com.formgenerator.domain.dto.response.TransactionStatusResponse;
import com.formgenerator.esign.model.ESignResponse;

public interface InvestmentOrchestrationService {

    /**
     * Initiates the fresh purchase flow:
     * 1. Validates and maps request
     * 2. Fills the AMC-specific PDF form
     * 3. Initiates Aadhaar eSign — returns a redirect URL
     *
     * @return response containing transactionId and eSign redirect URL
     */
    InvestmentResponse initiateFreshPurchase(InvestmentRequest request);

    /**
     * Completes the flow after eSign callback:
     * 1. Embeds PKCS#7 signature into the PDF
     * 2. Submits signed PDF to RTI
     *
     * @param transactionId the transactionId from the initial request
     * @param eSignResponse parsed eSign gateway response
     */
    void completeAfterESign(String transactionId, ESignResponse eSignResponse);

    /**
     * Returns the current status of a transaction.
     */
    TransactionStatusResponse getTransactionStatus(String transactionId);
}

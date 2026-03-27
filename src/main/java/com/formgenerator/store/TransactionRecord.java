package com.formgenerator.store;

import com.formgenerator.domain.model.InvestorDetails;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TransactionRecord {
    private String transactionId;
    private TransactionStatus status;
    private InvestorDetails investorDetails;
    /** Filled (but not yet signed) PDF bytes */
    private byte[] filledPdfBytes;
    /** Signed PDF bytes after eSign embedding */
    private byte[] signedPdfBytes;
    /** RTI acknowledgement reference number */
    private String rtiAcknowledgementNumber;
    private String eSignErrorCode;
    private String rtiErrorMessage;
    private Instant createdAt;
    private Instant updatedAt;
}

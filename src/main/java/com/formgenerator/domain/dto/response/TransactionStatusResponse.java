package com.formgenerator.domain.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class TransactionStatusResponse {
    String transactionId;
    String status;
    String amcName;
    String rtiAcknowledgementNumber;
    String eSignErrorCode;
    String rtiErrorMessage;
    Instant createdAt;
    Instant updatedAt;
}

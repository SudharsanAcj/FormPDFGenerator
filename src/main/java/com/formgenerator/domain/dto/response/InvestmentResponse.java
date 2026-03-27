package com.formgenerator.domain.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class InvestmentResponse {
    String transactionId;
    String status;
    /** URL to redirect user for Aadhaar eSign authentication */
    String eSignRedirectUrl;
    /** RTI acknowledgement number (populated after RTI submission) */
    String rtiAcknowledgementNumber;
    String amcName;
    Instant timestamp;
    String message;
}

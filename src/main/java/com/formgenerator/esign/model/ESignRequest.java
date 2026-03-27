package com.formgenerator.esign.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ESignRequest {
    String transactionId;
    String aspId;
    String authMode;       // "1"=OTP, "2"=Biometric
    String version;        // "2.1"
    String responseUrl;
    String documentHash;   // Base64 SHA-256 of the PDF
    String docInfo;        // Human-readable doc description
    String timestamp;      // ISO format
}

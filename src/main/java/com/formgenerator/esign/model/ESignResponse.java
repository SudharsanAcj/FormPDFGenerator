package com.formgenerator.esign.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ESignResponse {
    ESignStatus status;
    String transactionId;
    String errorCode;
    String errorMessage;
    /** PKCS#7 / CMS signature bytes (Base64-encoded) from the gateway */
    String pkcs7SignatureBase64;
    /** Signer's X.509 certificate (Base64-encoded DER) */
    String userCertificateBase64;
    String responseTimestamp;
}

package com.formgenerator.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMode {
    CHEQUE("CHEQUE"),
    DD("DD"),
    NEFT("NEFT"),
    RTGS("RTGS"),
    UPI("UPI"),
    NET_BANKING("NET_BANKING");

    private final String code;

    @JsonValue
    public String getCode() { return code; }

    @JsonCreator
    public static PaymentMode fromCode(String code) {
        for (PaymentMode p : values()) {
            if (p.code.equalsIgnoreCase(code)) return p;
        }
        throw new IllegalArgumentException("Unknown PaymentMode: " + code);
    }
}

package com.formgenerator.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransactionChargeType {
    FIRST_TIME_INVESTOR("FIRST_TIME_INVESTOR"),
    EXISTING_INVESTOR("EXISTING_INVESTOR");

    private final String code;

    @JsonValue
    public String getCode() { return code; }

    @JsonCreator
    public static TransactionChargeType fromCode(String code) {
        for (TransactionChargeType t : values()) {
            if (t.code.equalsIgnoreCase(code)) return t;
        }
        throw new IllegalArgumentException("Unknown TransactionChargeType: " + code);
    }
}

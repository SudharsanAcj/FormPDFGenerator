package com.formgenerator.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountType {
    SAVINGS("SAVINGS"),
    CURRENT("CURRENT"),
    NRE("NRE"),
    NRO("NRO"),
    FCNR("FCNR");

    private final String code;

    @JsonValue
    public String getCode() { return code; }

    @JsonCreator
    public static AccountType fromCode(String code) {
        for (AccountType a : values()) {
            if (a.code.equalsIgnoreCase(code)) return a;
        }
        throw new IllegalArgumentException("Unknown AccountType: " + code);
    }
}

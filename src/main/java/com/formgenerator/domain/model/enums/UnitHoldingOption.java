package com.formgenerator.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UnitHoldingOption {
    PHYSICAL("PHYSICAL"),
    DEMAT("DEMAT");

    private final String code;

    @JsonValue
    public String getCode() { return code; }

    @JsonCreator
    public static UnitHoldingOption fromCode(String code) {
        for (UnitHoldingOption u : values()) {
            if (u.code.equalsIgnoreCase(code)) return u;
        }
        throw new IllegalArgumentException("Unknown UnitHoldingOption: " + code);
    }
}

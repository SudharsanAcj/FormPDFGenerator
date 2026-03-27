package com.formgenerator.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ModeOfHolding {
    SINGLE("SINGLE"),
    JOINT("JOINT"),
    ANYONE_OR_SURVIVOR("ANYONE_OR_SURVIVOR");

    private final String code;

    @JsonValue
    public String getCode() { return code; }

    @JsonCreator
    public static ModeOfHolding fromCode(String code) {
        for (ModeOfHolding m : values()) {
            if (m.code.equalsIgnoreCase(code)) return m;
        }
        throw new IllegalArgumentException("Unknown ModeOfHolding: " + code);
    }
}

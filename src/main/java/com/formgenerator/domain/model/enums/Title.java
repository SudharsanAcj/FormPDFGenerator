package com.formgenerator.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Title {
    MR("MR"),
    MRS("MRS"),
    MS("MS"),
    DR("DR"),
    M_S("M/S");

    private final String code;

    @JsonValue
    public String getCode() { return code; }

    @JsonCreator
    public static Title fromCode(String code) {
        for (Title t : values()) {
            if (t.code.equalsIgnoreCase(code)) return t;
        }
        throw new IllegalArgumentException("Unknown Title: " + code);
    }
}

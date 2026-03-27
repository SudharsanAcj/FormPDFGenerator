package com.formgenerator.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("MALE"),
    FEMALE("FEMALE"),
    TRANSGENDER("TRANSGENDER");

    private final String code;

    @JsonValue
    public String getCode() { return code; }

    @JsonCreator
    public static Gender fromCode(String code) {
        for (Gender g : values()) {
            if (g.code.equalsIgnoreCase(code)) return g;
        }
        throw new IllegalArgumentException("Unknown Gender: " + code);
    }
}

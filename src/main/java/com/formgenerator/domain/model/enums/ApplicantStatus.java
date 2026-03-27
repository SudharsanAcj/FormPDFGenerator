package com.formgenerator.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicantStatus {
    INDIVIDUAL("INDIVIDUAL"),
    MINOR("MINOR"),
    HUF("HUF"),
    NRI("NRI"),
    COMPANY("COMPANY"),
    FII("FII"),
    TRUST("TRUST"),
    PARTNERSHIP("PARTNERSHIP"),
    BODY_CORPORATE("BODY_CORPORATE");

    private final String code;

    @JsonValue
    public String getCode() { return code; }

    @JsonCreator
    public static ApplicantStatus fromCode(String code) {
        for (ApplicantStatus s : values()) {
            if (s.code.equalsIgnoreCase(code)) return s;
        }
        throw new IllegalArgumentException("Unknown ApplicantStatus: " + code);
    }
}

package com.formgenerator.domain.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AmcName {
    BLACKROCK("BLACKROCK", "DSP BlackRock"),
    JIO_BLACKROCK("JIO_BLACKROCK", "Jio BlackRock Mutual Fund"),
    HDFC("HDFC", "HDFC Mutual Fund"),
    SBI("SBI", "SBI Mutual Fund"),
    ICICI("ICICI", "ICICI Prudential"),
    AXIS("AXIS", "Axis Mutual Fund"),
    NIPPON("NIPPON", "Nippon India Mutual Fund"),
    FRANKLIN("FRANKLIN", "Franklin Templeton");

    private final String code;
    private final String displayName;

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static AmcName fromCode(String code) {
        for (AmcName amc : values()) {
            if (amc.code.equalsIgnoreCase(code)) {
                return amc;
            }
        }
        throw new IllegalArgumentException("Unknown AMC code: " + code);
    }
}

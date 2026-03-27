package com.formgenerator.domain.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ESignAuthMode {
    OTP("1"),
    BIOMETRIC("2"),
    IRIS("3");

    private final String code;
}

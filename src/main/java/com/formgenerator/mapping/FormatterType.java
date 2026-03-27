package com.formgenerator.mapping;

public enum FormatterType {
    NONE,
    UPPER_CASE,
    TWO_DIGIT_PAD,   // Pad single digit with leading zero (e.g. 5 → "05")
    AMOUNT_IN_FIGURES,
    DATE_DD_MM_YYYY,
    TITLE_CASE
}

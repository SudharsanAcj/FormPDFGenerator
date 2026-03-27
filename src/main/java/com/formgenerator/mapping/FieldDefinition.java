package com.formgenerator.mapping;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FieldDefinition {

    /** Actual AcroForm field name as it appears in the PDF */
    private String pdfFieldName;

    /** How to treat this field when writing */
    private FieldType fieldType = FieldType.TEXT;

    /** Optional value formatter to apply before writing */
    private FormatterType formatter = FormatterType.NONE;

    /**
     * For CHECKBOX fields: the value to write when the domain field is true/matching.
     * Typically "Yes" or "On" depending on the PDF.
     */
    private String checkboxTrueValue = "Yes";

    /**
     * For RADIO_GROUP fields: the radio button export value to select.
     * E.g. "Male" for a gender radio group.
     */
    private String radioExportValue;

    /**
     * For DATE_PART fields: which part of a LocalDate this field represents.
     * Values: "DAY", "MONTH", "YEAR"
     */
    private String datePartRole;

    /**
     * Dot-notation path to the field on InvestorDetails (used for reflection-based value extraction).
     * E.g. "bankDetails.accountNumber", "nominees[0].nomineeName"
     */
    private String investorFieldPath;
}

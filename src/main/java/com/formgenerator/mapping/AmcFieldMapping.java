package com.formgenerator.mapping;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class AmcFieldMapping {

    /** AMC code matching AmcName enum */
    private String amcName;

    /** Classpath path to the PDF template, e.g. classpath:forms/BLACKROCK/fresh_purchase.pdf */
    private String pdfTemplatePath;

    /**
     * Key: canonical field identifier (matches investorFieldPath for clarity)
     * Value: FieldDefinition (pdfFieldName, fieldType, formatter, etc.)
     */
    private Map<String, FieldDefinition> fieldMappings;
}

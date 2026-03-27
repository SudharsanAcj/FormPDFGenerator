package com.formgenerator.mapping;

public enum FieldType {
    TEXT,
    CHECKBOX,
    RADIO_GROUP,
    DATE_PART,      // For split date fields (separate DD, MM, YYYY boxes)
    AMOUNT          // Numeric amount fields
}

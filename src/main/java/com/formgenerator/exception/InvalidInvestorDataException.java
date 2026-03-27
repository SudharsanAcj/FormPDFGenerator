package com.formgenerator.exception;

import java.util.List;

public class InvalidInvestorDataException extends RuntimeException {
    private final List<String> validationErrors;

    public InvalidInvestorDataException(List<String> validationErrors) {
        super("Investor data validation failed: " + String.join(", ", validationErrors));
        this.validationErrors = validationErrors;
    }

    public InvalidInvestorDataException(String message) {
        super(message);
        this.validationErrors = List.of(message);
    }

    public List<String> getValidationErrors() { return validationErrors; }
}

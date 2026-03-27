package com.formgenerator.exception;

public class RtiSubmissionException extends RuntimeException {
    private final String amcCode;

    public RtiSubmissionException(String amcCode, String message) {
        super("RTI submission failed for AMC [" + amcCode + "]: " + message);
        this.amcCode = amcCode;
    }

    public RtiSubmissionException(String amcCode, String message, Throwable cause) {
        super("RTI submission failed for AMC [" + amcCode + "]: " + message, cause);
        this.amcCode = amcCode;
    }

    public String getAmcCode() { return amcCode; }
}

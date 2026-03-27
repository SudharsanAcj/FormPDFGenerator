package com.formgenerator.exception;

public class ESignException extends RuntimeException {
    private final String errorCode;

    public ESignException(String message) {
        super(message);
        this.errorCode = "ESIGN_ERROR";
    }

    public ESignException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ESignException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ESIGN_ERROR";
    }

    public String getErrorCode() { return errorCode; }
}

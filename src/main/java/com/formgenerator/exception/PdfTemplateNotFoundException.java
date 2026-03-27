package com.formgenerator.exception;

public class PdfTemplateNotFoundException extends RuntimeException {
    public PdfTemplateNotFoundException(String amcCode, String path) {
        super("PDF template not found for AMC [" + amcCode + "] at path: " + path);
    }
}

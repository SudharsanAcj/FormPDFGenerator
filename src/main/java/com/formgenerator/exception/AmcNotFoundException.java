package com.formgenerator.exception;

public class AmcNotFoundException extends RuntimeException {
    public AmcNotFoundException(String amcCode) {
        super("No form-filler strategy registered for AMC: " + amcCode);
    }
}

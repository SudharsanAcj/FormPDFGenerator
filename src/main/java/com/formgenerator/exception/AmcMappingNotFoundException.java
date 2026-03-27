package com.formgenerator.exception;

public class AmcMappingNotFoundException extends RuntimeException {
    public AmcMappingNotFoundException(String amcCode) {
        super("Field mapping YAML not found for AMC: " + amcCode);
    }
}

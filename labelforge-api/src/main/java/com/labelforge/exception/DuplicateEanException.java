package com.labelforge.exception;

public class DuplicateEanException extends RuntimeException {
    private final String ean;

    public DuplicateEanException(String ean) {
        super("EAN already exists: " + ean);
        this.ean = ean;
    }

    public String getEan() { return ean; }
}

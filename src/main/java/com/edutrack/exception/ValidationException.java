package com.edutrack.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thrown when user input fails validation. Carries field-level messages so JSPs
 * can show errors next to the offending input (used by Member 01-06 module services).
 */
public class ValidationException extends RuntimeException {
    private final Map<String, String> fieldErrors = new LinkedHashMap<>();
    private final String generalError;

    public ValidationException(String generalError) {
        this.generalError = generalError;
    }

    public ValidationException(String field, String message) {
        this.generalError = null;
        fieldErrors.put(field, message);
    }

    public void add(String field, String message) {
        fieldErrors.merge(field, message, (a, b) -> a + " " + b);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public String getGeneralError() {
        return generalError;
    }

    public boolean hasFieldErrors() {
        return !fieldErrors.isEmpty();
    }
}

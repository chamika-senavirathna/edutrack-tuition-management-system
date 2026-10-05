package com.edutrack.exception;

/**
 * Thrown when an operation violates a documented business rule
 * (BR-REG-01..03, BR-TTC-01..03, BR-ATT-01..03, BR-FEE-01..03, BR-EXM-01..03).
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}

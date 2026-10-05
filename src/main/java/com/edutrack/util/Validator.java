package com.edutrack.util;

import com.edutrack.exception.ValidationException;

import java.util.regex.Pattern;

/**
 * Server-side validation (Section 15 of the project requirements).
 * Validation is ALWAYS performed on the server; JavaScript validation in the
 * browser is only a convenience and is never trusted.
 */
public final class Validator {

    private static final Pattern EMAIL =
            Pattern.compile("^[\\w.!#$%&'*+/=?^`{|}~-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern SRI_LANKA_PHONE = Pattern.compile("^0[0-9]{9}$");
    private static final Pattern NIC_OLD = Pattern.compile("^[0-9]{9}[VvXx]$");
    private static final Pattern NIC_NEW = Pattern.compile("^[0-9]{12}$");
    private static final Pattern USERNAME = Pattern.compile("^[a-zA-Z0-9._-]{4,40}$");

    private final com.edutrack.exception.ValidationException errors = new ValidationException("Validation failed");

    /** Record a custom field error directly. */
    public void add(String field, String message) {
        errors.add(field, message);
    }

    public Validator required(String value, String field, String label) {
        if (value == null || value.isBlank()) {
            errors.add(field, label + " is required.");
        }
        return this;
    }

    public Validator maxLength(String value, String field, String label, int max) {
        if (value != null && value.length() > max) {
            errors.add(field, label + " must not exceed " + max + " characters.");
        }
        return this;
    }

    public Validator email(String value, String field, String label) {
        if (value != null && !value.isBlank() && !EMAIL.matcher(value.trim()).matches()) {
            errors.add(field, label + " must be a valid email address.");
        }
        return this;
    }

    public Validator phone(String value, String field, String label) {
        if (value != null && !value.isBlank() && !SRI_LANKA_PHONE.matcher(value.trim()).matches()) {
            errors.add(field, label + " must be a valid 10-digit phone number (e.g. 0771234567).");
        }
        return this;
    }

    /** NIC in old (9 digits + V/X) or new (12 digits) Sri Lankan format. */
    public Validator nic(String value, String field, String label) {
        if (value != null && !value.isBlank()) {
            String v = value.trim();
            if (!NIC_OLD.matcher(v).matches() && !NIC_NEW.matcher(v).matches()) {
                errors.add(field, label + " must be a valid NIC (e.g. 950123456V or 199501234567).");
            }
        }
        return this;
    }

    public Validator username(String value, String field, String label) {
        if (value != null && !value.isBlank() && !USERNAME.matcher(value.trim()).matches()) {
            errors.add(field, label + " must be 4-40 characters (letters, digits, dot, dash, underscore).");
        }
        return this;
    }

    public Validator positiveNumber(String value, String field, String label) {
        if (value != null && !value.isBlank()) {
            try {
                double d = Double.parseDouble(value.trim());
                if (d <= 0) {
                    errors.add(field, label + " must be greater than zero.");
                }
            } catch (NumberFormatException e) {
                errors.add(field, label + " must be a valid number.");
            }
        }
        return this;
    }

    public Validator intRange(String value, String field, String label, int min, int max) {
        if (value != null && !value.isBlank()) {
            try {
                int i = Integer.parseInt(value.trim());
                if (i < min || i > max) {
                    errors.add(field, label + " must be between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                errors.add(field, label + " must be a whole number.");
            }
        }
        return this;
    }

    public Validator strongPassword(String value, String field, String label) {
        if (value != null && (value.length() < 8
                || !value.matches(".*[A-Za-z].*") || !value.matches(".*[0-9].*"))) {
            errors.add(field, label + " must be at least 8 characters and contain letters and numbers.");
        }
        return this;
    }

    /** Throws ValidationException if any rule recorded a message. */
    public void check() {
        if (errors.hasFieldErrors()) {
            throw errors;
        }
    }

    public boolean failed() {
        return errors.hasFieldErrors();
    }

    public ValidationException exception() {
        return errors;
    }
}

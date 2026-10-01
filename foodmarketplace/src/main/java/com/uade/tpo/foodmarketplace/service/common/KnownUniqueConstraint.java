package com.uade.tpo.foodmarketplace.service.common;

import org.springframework.dao.DataIntegrityViolationException;

/** Classifies only explicitly named constraints, never generic duplicate-key text. */
public final class KnownUniqueConstraint {
    private KnownUniqueConstraint() {
    }

    public static boolean matches(DataIntegrityViolationException exception, String expectedName) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof org.hibernate.exception.ConstraintViolationException violation) {
                String actualName = violation.getConstraintName();
                if (actualName != null) {
                    return expectedName.equals(normalizeConstraintName(actualName));
                }
            }
        }
        return false;
    }

    private static String normalizeConstraintName(String name) {
        String unquoted = name.replace("`", "").replace("\"", "");
        return unquoted.substring(unquoted.lastIndexOf('.') + 1);
    }
}

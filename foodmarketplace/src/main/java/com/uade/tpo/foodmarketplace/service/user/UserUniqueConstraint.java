package com.uade.tpo.foodmarketplace.service.user;

import org.springframework.dao.DataIntegrityViolationException;

import com.uade.tpo.foodmarketplace.exceptions.user.UserDuplicateException;
import com.uade.tpo.foodmarketplace.service.common.KnownUniqueConstraint;

/** Traduce solo las dos constraints de unicidad conocidas de User. */
public final class UserUniqueConstraint {
    private UserUniqueConstraint() { }

    public static RuntimeException translate(DataIntegrityViolationException exception) {
        if (KnownUniqueConstraint.matches(exception, "uk_user_username")
                || KnownUniqueConstraint.matches(exception, "uk_user_email")) {
            return new UserDuplicateException();
        }
        return exception;
    }
}

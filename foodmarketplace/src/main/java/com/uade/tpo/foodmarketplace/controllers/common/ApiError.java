package com.uade.tpo.foodmarketplace.controllers.common;
import java.time.LocalDateTime;

public record ApiError(
        boolean success,
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Object data,
        String path) {
}

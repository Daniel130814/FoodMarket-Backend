package com.uade.tpo.foodmarketplace.controllers;
import java.time.LocalDateTime;

public record ApiError(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path) {
}

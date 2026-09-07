package com.uade.tpo.foodmarketplace.entity.dto.common;

/**
 * Envuelve respuestas exitosas cuando el frontend necesita contexto adicional
 * sobre el resultado, incluso si una colección está vacía.
 */
public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
}

package com.uade.tpo.foodmarketplace.controllers;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.uade.tpo.foodmarketplace.exceptions.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.CalificacionInvalidaException;
import com.uade.tpo.foodmarketplace.exceptions.CantidadInvalidaException;
import com.uade.tpo.foodmarketplace.exceptions.CategoryDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.CategoryNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.DomicilioNoPerteneceAlUsuarioException;
import com.uade.tpo.foodmarketplace.exceptions.DomicilioNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.IngredienteNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.PagoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.PedidoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.PlatoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.ResenaDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.UserDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.UserNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ BusinessRuleException.class, CantidadInvalidaException.class,
            CalificacionInvalidaException.class, CategoryDuplicateException.class, UserDuplicateException.class })
    ResponseEntity<ApiError> badRequest(RuntimeException ex, WebRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler({ResenaDuplicateException.class})
    ResponseEntity<ApiError> conflict(RuntimeException ex, WebRequest request) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({ UserNotFoundException.class, PlatoNotFoundException.class, PedidoNotFoundException.class,
            PagoNotFoundException.class, DomicilioNotFoundException.class, IngredienteNotFoundException.class,
            CategoryNotFoundException.class })
    ResponseEntity<ApiError> notFound(RuntimeException ex, WebRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DomicilioNoPerteneceAlUsuarioException.class)
    ResponseEntity<ApiError> forbidden(RuntimeException ex, WebRequest request) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String message, WebRequest request) {
        ApiError apiError = new ApiError(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message,
                request.getDescription(false).replace("uri=", ""));

        return ResponseEntity.status(status).body(apiError);
    }
}

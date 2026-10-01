package com.uade.tpo.foodmarketplace.controllers.common;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.uade.tpo.foodmarketplace.exceptions.common.BusinessRuleException;
import com.uade.tpo.foodmarketplace.exceptions.common.ResourceInUseException;
import com.uade.tpo.foodmarketplace.exceptions.resena.CalificacionInvalidaException;
import com.uade.tpo.foodmarketplace.exceptions.order.CantidadInvalidaException;
import com.uade.tpo.foodmarketplace.exceptions.category.CategoryDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.category.CategoryNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.chefprofile.ChefProfileNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.domicilio.DomicilioNoPerteneceAlUsuarioException;
import com.uade.tpo.foodmarketplace.exceptions.domicilio.DomicilioNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.ingrediente.IngredienteNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.ingrediente.IngredienteDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.pago.PagoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.order.PedidoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.order.SubPedidoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.order.InvalidOrderStateException;
import com.uade.tpo.foodmarketplace.exceptions.order.InvalidSubPedidoStateException;
import com.uade.tpo.foodmarketplace.exceptions.order.OrderCancelledException;
import com.uade.tpo.foodmarketplace.exceptions.pago.InvalidPagoStateException;
import com.uade.tpo.foodmarketplace.exceptions.plato.PlatoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.resena.ResenaDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.resena.ResenaNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserDuplicateException;
import com.uade.tpo.foodmarketplace.exceptions.user.UserNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.carrito.CarritoVacioException;
import com.uade.tpo.foodmarketplace.exceptions.carrito.ItemCarritoNotFoundException;
import com.uade.tpo.foodmarketplace.exceptions.carrito.CarritoIntegrityConflictException;
import com.uade.tpo.foodmarketplace.entity.dto.common.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ BusinessRuleException.class, CantidadInvalidaException.class,
            CalificacionInvalidaException.class,
            UserDuplicateException.class, CarritoVacioException.class })
    ResponseEntity<ApiResponse<Void>> badRequest(RuntimeException ex, WebRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler({ ResenaDuplicateException.class, ResourceInUseException.class, InvalidOrderStateException.class,
            InvalidSubPedidoStateException.class, InvalidPagoStateException.class, OrderCancelledException.class,
            CarritoIntegrityConflictException.class, CategoryDuplicateException.class, IngredienteDuplicateException.class })
    ResponseEntity<ApiResponse<Void>> conflict(RuntimeException ex, WebRequest request) {
        return error(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler({ UserNotFoundException.class, PlatoNotFoundException.class, PedidoNotFoundException.class,
            PagoNotFoundException.class, DomicilioNotFoundException.class, IngredienteNotFoundException.class,
            CategoryNotFoundException.class, ResenaNotFoundException.class, ChefProfileNotFoundException.class,
            SubPedidoNotFoundException.class, ItemCarritoNotFoundException.class })
    ResponseEntity<ApiResponse<Void>> notFound(RuntimeException ex, WebRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DomicilioNoPerteneceAlUsuarioException.class)
    ResponseEntity<ApiResponse<Void>> forbidden(RuntimeException ex, WebRequest request) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> accessDenied(AccessDeniedException ex, WebRequest request) {
        return error(HttpStatus.FORBIDDEN, "No tenés permisos para realizar esta operación", request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiResponse<Void>> unauthorized(BadCredentialsException ex, WebRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "Necesitás iniciar sesión para realizar esta operación", request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException ex, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> malformedRequest(HttpMessageNotReadableException ex, WebRequest request) {
        return error(HttpStatus.BAD_REQUEST, "La solicitud contiene un valor inválido", request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> unexpected(Exception ex, WebRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno al procesar la solicitud", request);
    }

    private ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message, WebRequest request) {
        return ResponseEntity.status(status).body(ApiResponse.error(message));
    }
}

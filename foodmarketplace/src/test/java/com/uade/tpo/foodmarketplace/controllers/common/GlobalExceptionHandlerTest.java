package com.uade.tpo.foodmarketplace.controllers.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import com.uade.tpo.foodmarketplace.exceptions.resena.ResenaDuplicateException;

class GlobalExceptionHandlerTest {

    @Test
    void errorDeResenaDuplicadaEsUnConflictAmigableParaFrontend() {
        WebRequest request = Mockito.mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/resenas/createResena");

        var response = new GlobalExceptionHandler().conflict(new ResenaDuplicateException(), request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals(ResenaDuplicateException.MESSAGE, response.getBody().message());
        assertNull(response.getBody().data());
    }
}

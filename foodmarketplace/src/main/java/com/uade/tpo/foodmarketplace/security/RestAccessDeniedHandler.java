package com.uade.tpo.foodmarketplace.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException exception) throws IOException {
        RestAuthenticationEntryPoint.write(response, request, HttpServletResponse.SC_FORBIDDEN,
                "Forbidden", "No tenés permisos para acceder a este recurso");
    }
}

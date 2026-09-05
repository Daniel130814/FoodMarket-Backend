package com.uade.tpo.foodmarketplace.security;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        write(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                "Se requiere autenticación válida");
    }

    static void write(HttpServletResponse response, HttpServletRequest request, int status,
            String error, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now()
                + "\",\"status\":" + status + ",\"error\":\"" + error
                + "\",\"message\":\"" + message + "\",\"path\":\""
                + request.getRequestURI() + "\"}");
    }
}

package com.uade.tpo.foodmarketplace.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import io.jsonwebtoken.JwtException;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @Test
    void generaYValidaTokenConEmailComoSubject() {
        JwtService service = new JwtService(SECRET, 60_000);
        var user = User.withUsername("cliente@mail.com").password("hash").authorities("CLIENTE").build();

        String token = service.generateToken(user);

        assertEquals("cliente@mail.com", service.extractUsername(token));
        assertTrue(service.isTokenValid(token, user));
    }

    @Test
    void tokenNoPerteneceAOtroUsuario() {
        JwtService service = new JwtService(SECRET, 60_000);
        var owner = User.withUsername("a@mail.com").password("hash").authorities("CLIENTE").build();
        var other = User.withUsername("b@mail.com").password("hash").authorities("CLIENTE").build();

        assertFalse(service.isTokenValid(service.generateToken(owner), other));
    }

    @Test
    void rechazaFirmaInvalida() {
        JwtService issuer = new JwtService(SECRET, 60_000);
        JwtService verifier = new JwtService("abcdef0123456789abcdef0123456789", 60_000);
        var user = User.withUsername("a@mail.com").password("hash").authorities("CLIENTE").build();

        assertThrows(JwtException.class, () -> verifier.extractUsername(issuer.generateToken(user)));
    }
}

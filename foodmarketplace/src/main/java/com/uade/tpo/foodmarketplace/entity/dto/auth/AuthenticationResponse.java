package com.uade.tpo.foodmarketplace.entity.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uade.tpo.foodmarketplace.entity.user.Role;

public record AuthenticationResponse(
        @JsonProperty("access_token") String accessToken,
        Long userId,
        Role role) {
}

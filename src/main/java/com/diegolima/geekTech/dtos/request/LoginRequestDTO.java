package com.diegolima.geekTech.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for user login")
public record LoginRequestDTO(
        @Schema(description = "Registered user email address", example = "exemplo@gmail.com")
        String email,

        @Schema(description = "User password", example = "password123")
        String password
) {
}

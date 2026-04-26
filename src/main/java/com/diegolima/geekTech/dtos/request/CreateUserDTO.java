package com.diegolima.geekTech.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for user registration")
public record CreateUserDTO(
        @Schema(description = "User display name", example = "Diego Lima")
        String name,

        @Schema(description = "User email address", example = "exemplo@gmail.com")
        String email,

        @Schema(description = "Raw password to be encrypted before storage", example = "password123")
        String password
) {
}

package com.diegolima.geekTech.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Public user response")
public record UserResponseDTO(
        @Schema(description = "User display name", example = "Diego Lima")
        String name,

        @Schema(description = "User email address", example = "exemplo@gmail.com")
        String email
) {
}

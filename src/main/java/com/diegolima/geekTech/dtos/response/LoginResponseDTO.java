package com.diegolima.geekTech.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after successful authentication")
public record LoginResponseDTO(
        @Schema(description = "Authenticated user id", example = "1")
        Long id,

        @Schema(description = "Authenticated user email address", example = "exemplo@gmail.com")
        String email,

        @Schema(description = "Issued token details")
        TokenResponseDTO token
) {
}

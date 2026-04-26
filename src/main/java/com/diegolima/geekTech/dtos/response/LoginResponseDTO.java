package com.diegolima.geekTech.dtos.response;

public record LoginResponseDTO(
        Long id,
        String email,
        TokenResponseDTO token
) {
}

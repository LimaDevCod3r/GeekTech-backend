package com.diegolima.geekTech.dtos.request;

public record CreateUserDTO(
        String name,
        String email,
        String password
) {
}

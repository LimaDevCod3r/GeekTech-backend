package com.diegolima.geekTech.controller;

import com.diegolima.geekTech.dtos.request.CreateUserDTO;
import com.diegolima.geekTech.dtos.request.LoginRequestDTO;
import com.diegolima.geekTech.dtos.response.LoginResponseDTO;
import com.diegolima.geekTech.dtos.response.UserResponseDTO;
import com.diegolima.geekTech.usecase.AuthUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody CreateUserDTO userDTO) {
        var response = authUseCase.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        var response = authUseCase.login(request);
        return ResponseEntity.ok(response);
    }
}

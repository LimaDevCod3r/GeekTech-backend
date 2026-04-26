package com.diegolima.geekTech.controller;

import com.diegolima.geekTech.dtos.request.CreateUserDTO;
import com.diegolima.geekTech.dtos.request.LoginRequestDTO;
import com.diegolima.geekTech.dtos.response.LoginResponseDTO;
import com.diegolima.geekTech.dtos.response.UserResponseDTO;
import com.diegolima.geekTech.usecase.AuthUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a user", description = "Creates a new user account with the default USER role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created",
                    content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or email already registered",
                    content = @Content)
    })
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody CreateUserDTO userDTO) {
        var response = authUseCase.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate a user", description = "Authenticates valid credentials and returns an access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication succeeded",
                    content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content)
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        var response = authUseCase.login(request);
        return ResponseEntity.ok(response);
    }
}

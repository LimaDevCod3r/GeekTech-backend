package com.diegolima.geekTech.usecase;

import com.diegolima.geekTech.dtos.request.CreateUserDTO;
import com.diegolima.geekTech.dtos.request.LoginRequestDTO;
import com.diegolima.geekTech.dtos.response.LoginResponseDTO;
import com.diegolima.geekTech.dtos.response.TokenResponseDTO;
import com.diegolima.geekTech.dtos.response.UserResponseDTO;
import com.diegolima.geekTech.models.Role;
import com.diegolima.geekTech.models.User;
import com.diegolima.geekTech.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserResponseDTO createUser(CreateUserDTO userDTO) {
        validateEmailIsAvailable(userDTO.email());

        var user = buildUser(userDTO);
        var savedUser = userRepository.save(user);

        return toResponse(savedUser);
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        var user = userRepository.findByEmail(loginRequestDTO.email())
                .orElseThrow(
                        () -> new RuntimeException("Invalid credentials.")
                );

        if (!passwordEncoder.matches(loginRequestDTO.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials.");
        }

        var token = jwtService.generateToken(user);

        return new LoginResponseDTO(
                user.getId(),
                user.getEmail(),
                new TokenResponseDTO(token)
        );
    }


    private void validateEmailIsAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
    }

    private User buildUser(CreateUserDTO userDTO) {
        return User.builder()
                .name(userDTO.name())
                .email(userDTO.email())
                .password(passwordEncoder.encode(userDTO.password()))
                .role(Role.USER)
                .build();
    }

    private UserResponseDTO toResponse(User savedUser) {
        return new UserResponseDTO(
                savedUser.getName(),
                savedUser.getEmail()
        );
    }
}

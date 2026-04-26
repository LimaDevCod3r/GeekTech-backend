package com.diegolima.geekTech.config;

import com.diegolima.geekTech.models.Role;
import com.diegolima.geekTech.models.User;
import com.diegolima.geekTech.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.name}")
    private String name;

    @Value("${admin.email}")
    private String email;

    @Value("${admin.password}")
    private String password;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void run(String... args) throws Exception {

        if (!userRepository.existsByEmail(email)) {
            var admin = User.builder()
                    .name(name)
                    .email(email)
                    .password(passwordEncoder.encode(password))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            System.out.println(">>> Admin user created successfully.");

        }
    }

}

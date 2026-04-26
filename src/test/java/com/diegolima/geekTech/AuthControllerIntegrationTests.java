package com.diegolima.geekTech;

import com.diegolima.geekTech.models.Role;
import com.diegolima.geekTech.models.User;
import com.diegolima.geekTech.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createsUserWhenRequestIsValid() throws Exception {
        userRepository.deleteAll();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Diego Lima",
                                  "email": "diego@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Diego Lima"))
                .andExpect(jsonPath("$.email").value("diego@example.com"));

        var savedUser = userRepository.findByEmail("diego@example.com").orElseThrow();

        assertThat(savedUser.getName()).isEqualTo("Diego Lima");
        assertThat(savedUser.getRole()).isEqualTo(Role.USER);
        assertThat(savedUser.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", savedUser.getPassword())).isTrue();
    }

    @Test
    void logsInUserWhenCredentialsAreValid() throws Exception {
        userRepository.deleteAll();

        var user = userRepository.save(User.builder()
                .name("Example User")
                .email("exemplo@gmail.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "exemplo@gmail.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value("exemplo@gmail.com"))
                .andExpect(jsonPath("$.token.accessToken").isNotEmpty());
    }

    @Test
    void exposesAuthenticationEndpointsInOpenApiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("GeekTech API"))
                .andExpect(jsonPath("$.paths['/auth/register']").exists())
                .andExpect(jsonPath("$.paths['/auth/login']").exists());
    }
}

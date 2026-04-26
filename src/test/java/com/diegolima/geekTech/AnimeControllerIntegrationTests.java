package com.diegolima.geekTech;

import com.diegolima.geekTech.models.Role;
import com.diegolima.geekTech.models.User;
import com.diegolima.geekTech.repository.AnimeRepository;
import com.diegolima.geekTech.repository.UserRepository;
import com.diegolima.geekTech.services.CloudinaryService;
import com.diegolima.geekTech.services.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnimeControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnimeRepository animeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @AfterEach
    void cleanDatabase() {
        animeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createsAnimeAndPersistsItWhenRequestIsValid() throws Exception {
        animeRepository.deleteAll();
        userRepository.deleteAll();

        var admin = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin-anime@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        when(cloudinaryService.upload(any(), eq("geektech/animes")))
                .thenReturn(new CloudinaryService.CloudinaryUploadResponse(
                        "geektech/animes/fullmetal",
                        "https://res.cloudinary.com/demo/image/upload/v1/geektech/animes/fullmetal.jpg"
                ));

        var photo = new MockMultipartFile(
                "photo",
                "fullmetal.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/anime")
                        .file(photo)
                        .param("name", "Fullmetal Alchemist: Brotherhood")
                        .param("synopsis", "Two brothers search for the Philosopher's Stone.")
                        .param("created_by_user_id", admin.getId().toString())
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Fullmetal Alchemist: Brotherhood"))
                .andExpect(jsonPath("$.synopsis").value("Two brothers search for the Philosopher's Stone."))
                .andExpect(jsonPath("$.photo").value("https://res.cloudinary.com/demo/image/upload/v1/geektech/animes/fullmetal.jpg"));

        var savedAnime = animeRepository.findAll();

        assertThat(savedAnime).hasSize(1);
        assertThat(savedAnime.getFirst().getName()).isEqualTo("Fullmetal Alchemist: Brotherhood");
        assertThat(savedAnime.getFirst().getSynopsis()).isEqualTo("Two brothers search for the Philosopher's Stone.");
        assertThat(savedAnime.getFirst().getPhoto()).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1/geektech/animes/fullmetal.jpg");
        assertThat(savedAnime.getFirst().getCreatedBy().getId()).isEqualTo(admin.getId());
    }

    @TestConfiguration
    static class CloudinaryTestConfiguration {

        @Bean
        @Primary
        CloudinaryService cloudinaryService() {
            return Mockito.mock(CloudinaryService.class);
        }
    }
}

package com.diegolima.geekTech;

import com.diegolima.geekTech.models.Role;
import com.diegolima.geekTech.models.Anime;
import com.diegolima.geekTech.models.User;
import com.diegolima.geekTech.dtos.response.CloudinaryUploadResponse;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        Mockito.reset(cloudinaryService);
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
                .thenReturn(new CloudinaryUploadResponse(
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
        assertThat(savedAnime.getFirst().getPhotoPublicId()).isEqualTo("geektech/animes/fullmetal");
        assertThat(savedAnime.getFirst().getCreatedBy().getId()).isEqualTo(admin.getId());
    }

    @Test
    void findsAnimeByIdWhenAnimeExists() throws Exception {
        animeRepository.deleteAll();
        userRepository.deleteAll();

        var admin = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin-find-anime@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());

        var anime = animeRepository.saveAndFlush(Anime.builder()
                .name("Naruto")
                .synopsis("A young ninja seeks recognition from his village.")
                .photo("https://res.cloudinary.com/demo/image/upload/v1/geektech/animes/naruto.jpg")
                .photoPublicId("geektech/animes/naruto")
                .createdBy(admin)
                .build());

        mockMvc.perform(get("/anime/{id}", anime.getId())
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(anime.getId()))
                .andExpect(jsonPath("$.name").value("Naruto"))
                .andExpect(jsonPath("$.synopsis").value("A young ninja seeks recognition from his village."))
                .andExpect(jsonPath("$.photo").value("https://res.cloudinary.com/demo/image/upload/v1/geektech/animes/naruto.jpg"));
    }

    @Test
    void findsAllAnimesWithPagination() throws Exception {
        var admin = createAdmin("admin-list-anime@example.com");

        saveAnime("Naruto", "A young ninja seeks recognition from his village.", "naruto", admin);
        saveAnime("Bleach", "A teenager becomes a substitute Soul Reaper.", "bleach", admin);

        mockMvc.perform(get("/anime")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "name,asc")
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Bleach"))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void searchesAnimesByNameWithPagination() throws Exception {
        var admin = createAdmin("admin-search-anime@example.com");

        saveAnime("Naruto", "A young ninja seeks recognition from his village.", "naruto", admin);
        saveAnime("Bleach", "A teenager becomes a substitute Soul Reaper.", "bleach", admin);

        mockMvc.perform(get("/anime/search")
                        .param("name", "nar")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Naruto"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void updatesAnimeAndReplacesCloudinaryPhotoWhenNewPhotoIsSent() throws Exception {
        var admin = createAdmin("admin-update-anime@example.com");
        var anime = saveAnime("Naruto", "Original synopsis.", "naruto-old", admin);

        when(cloudinaryService.upload(any(), eq("geektech/animes")))
                .thenReturn(new CloudinaryUploadResponse(
                        "geektech/animes/naruto-new",
                        "https://res.cloudinary.com/demo/image/upload/v1/geektech/animes/naruto-new.jpg"
                ));

        var photo = new MockMultipartFile(
                "photo",
                "naruto-new.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "new-fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/anime/{id}", anime.getId())
                        .file(photo)
                        .param("name", "Naruto Shippuden")
                        .param("synopsis", "Updated synopsis.")
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(anime.getId()))
                .andExpect(jsonPath("$.name").value("Naruto Shippuden"))
                .andExpect(jsonPath("$.synopsis").value("Updated synopsis."))
                .andExpect(jsonPath("$.photo").value("https://res.cloudinary.com/demo/image/upload/v1/geektech/animes/naruto-new.jpg"));

        var updatedAnime = animeRepository.findById(anime.getId()).orElseThrow();

        assertThat(updatedAnime.getName()).isEqualTo("Naruto Shippuden");
        assertThat(updatedAnime.getSynopsis()).isEqualTo("Updated synopsis.");
        assertThat(updatedAnime.getPhoto()).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1/geektech/animes/naruto-new.jpg");
        assertThat(updatedAnime.getPhotoPublicId()).isEqualTo("geektech/animes/naruto-new");
        verify(cloudinaryService).delete("geektech/animes/naruto-old");
    }

    @Test
    void deletesAnimeAndRemovesCloudinaryPhoto() throws Exception {
        var admin = createAdmin("admin-delete-anime@example.com");
        var anime = saveAnime("Naruto", "A young ninja seeks recognition from his village.", "naruto", admin);

        mockMvc.perform(delete("/anime/{id}", anime.getId())
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isNoContent());

        assertThat(animeRepository.findById(anime.getId())).isEmpty();
        verify(cloudinaryService).delete("geektech/animes/naruto");
    }

    private User createAdmin(String email) {
        return userRepository.save(User.builder()
                .name("Admin User")
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());
    }

    private Anime saveAnime(String name, String synopsis, String imageName, User admin) {
        return animeRepository.saveAndFlush(Anime.builder()
                .name(name)
                .synopsis(synopsis)
                .photo("https://res.cloudinary.com/demo/image/upload/v1/geektech/animes/" + imageName + ".jpg")
                .photoPublicId("geektech/animes/" + imageName)
                .createdBy(admin)
                .build());
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

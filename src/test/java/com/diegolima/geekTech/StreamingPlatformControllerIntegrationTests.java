package com.diegolima.geekTech;

import com.diegolima.geekTech.dtos.response.CloudinaryUploadResponse;
import com.diegolima.geekTech.models.Role;
import com.diegolima.geekTech.models.StreamingPlatform;
import com.diegolima.geekTech.models.User;
import com.diegolima.geekTech.repository.StreamingPlatformRepository;
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
class StreamingPlatformControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StreamingPlatformRepository streamingPlatformRepository;

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
        streamingPlatformRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createsStreamingPlatformAndPersistsItWhenRequestIsValid() throws Exception {
        var admin = createAdmin("admin-platform@example.com");

        when(cloudinaryService.upload(any(), eq("geektech/streaming-platforms")))
                .thenReturn(new CloudinaryUploadResponse(
                        "geektech/streaming-platforms/crunchyroll",
                        "https://res.cloudinary.com/demo/image/upload/v1/geektech/streaming-platforms/crunchyroll.png"
                ));

        var logo = new MockMultipartFile(
                "logo",
                "crunchyroll.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-logo-content".getBytes()
        );

        mockMvc.perform(multipart("/platform")
                        .file(logo)
                        .param("name", "Crunchyroll")
                        .param("websiteUrl", "https://www.crunchyroll.com")
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Crunchyroll"))
                .andExpect(jsonPath("$.websiteUrl").value("https://www.crunchyroll.com"))
                .andExpect(jsonPath("$.logo").value("https://res.cloudinary.com/demo/image/upload/v1/geektech/streaming-platforms/crunchyroll.png"));

        var savedPlatforms = streamingPlatformRepository.findAll();

        assertThat(savedPlatforms).hasSize(1);
        assertThat(savedPlatforms.getFirst().getName()).isEqualTo("Crunchyroll");
        assertThat(savedPlatforms.getFirst().getWebsiteUrl()).isEqualTo("https://www.crunchyroll.com");
        assertThat(savedPlatforms.getFirst().getLogo()).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1/geektech/streaming-platforms/crunchyroll.png");
        assertThat(savedPlatforms.getFirst().getLogoPublicId()).isEqualTo("geektech/streaming-platforms/crunchyroll");
    }

    @Test
    void returnsUnauthorizedWhenCreatingStreamingPlatformWithoutAuthentication() throws Exception {
        var logo = new MockMultipartFile(
                "logo",
                "crunchyroll.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-logo-content".getBytes()
        );

        mockMvc.perform(multipart("/platform")
                        .file(logo)
                        .param("name", "Crunchyroll")
                        .param("websiteUrl", "https://www.crunchyroll.com"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void findsAllStreamingPlatformsWithPagination() throws Exception {
        var admin = createAdmin("admin-list-platform@example.com");

        savePlatform("Netflix", "https://www.netflix.com", "netflix");
        savePlatform("Crunchyroll", "https://www.crunchyroll.com", "crunchyroll");

        mockMvc.perform(get("/platform")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "name,asc")
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Crunchyroll"))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void searchesStreamingPlatformsByNameWithPagination() throws Exception {
        var admin = createAdmin("admin-search-platform@example.com");

        savePlatform("Netflix", "https://www.netflix.com", "netflix");
        savePlatform("Crunchyroll", "https://www.crunchyroll.com", "crunchyroll");

        mockMvc.perform(get("/platform/search")
                        .param("name", "crunch")
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Crunchyroll"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findsStreamingPlatformByIdWhenPlatformExists() throws Exception {
        var admin = createAdmin("admin-find-platform@example.com");
        var platform = savePlatform("Crunchyroll", "https://www.crunchyroll.com", "crunchyroll");

        mockMvc.perform(get("/platform/{id}", platform.getId())
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(platform.getId()))
                .andExpect(jsonPath("$.name").value("Crunchyroll"))
                .andExpect(jsonPath("$.websiteUrl").value("https://www.crunchyroll.com"))
                .andExpect(jsonPath("$.logo").value("https://res.cloudinary.com/demo/image/upload/v1/geektech/streaming-platforms/crunchyroll.png"));
    }

    @Test
    void updatesStreamingPlatformAndReplacesCloudinaryLogoWhenNewLogoIsSent() throws Exception {
        var admin = createAdmin("admin-update-platform@example.com");
        var platform = savePlatform("Crunchyroll", "https://old.crunchyroll.com", "crunchyroll-old");

        when(cloudinaryService.upload(any(), eq("geektech/streaming-platforms")))
                .thenReturn(new CloudinaryUploadResponse(
                        "geektech/streaming-platforms/crunchyroll-new",
                        "https://res.cloudinary.com/demo/image/upload/v1/geektech/streaming-platforms/crunchyroll-new.png"
                ));

        var logo = new MockMultipartFile(
                "logo",
                "crunchyroll-new.png",
                MediaType.IMAGE_PNG_VALUE,
                "new-fake-logo-content".getBytes()
        );

        mockMvc.perform(multipart("/platform/{id}", platform.getId())
                        .file(logo)
                        .param("name", "Crunchyroll")
                        .param("websiteUrl", "https://www.crunchyroll.com")
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(platform.getId()))
                .andExpect(jsonPath("$.name").value("Crunchyroll"))
                .andExpect(jsonPath("$.websiteUrl").value("https://www.crunchyroll.com"))
                .andExpect(jsonPath("$.logo").value("https://res.cloudinary.com/demo/image/upload/v1/geektech/streaming-platforms/crunchyroll-new.png"));

        var updatedPlatform = streamingPlatformRepository.findById(platform.getId()).orElseThrow();

        assertThat(updatedPlatform.getWebsiteUrl()).isEqualTo("https://www.crunchyroll.com");
        assertThat(updatedPlatform.getLogo()).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1/geektech/streaming-platforms/crunchyroll-new.png");
        assertThat(updatedPlatform.getLogoPublicId()).isEqualTo("geektech/streaming-platforms/crunchyroll-new");
        verify(cloudinaryService).delete("geektech/streaming-platforms/crunchyroll-old");
    }

    @Test
    void deletesStreamingPlatformAndRemovesCloudinaryLogo() throws Exception {
        var admin = createAdmin("admin-delete-platform@example.com");
        var platform = savePlatform("Crunchyroll", "https://www.crunchyroll.com", "crunchyroll");

        mockMvc.perform(delete("/platform/{id}", platform.getId())
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isNoContent());

        assertThat(streamingPlatformRepository.findById(platform.getId())).isEmpty();
        verify(cloudinaryService).delete("geektech/streaming-platforms/crunchyroll");
    }

    private User createAdmin(String email) {
        return userRepository.save(User.builder()
                .name("Admin User")
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());
    }

    private StreamingPlatform savePlatform(String name, String websiteUrl, String logoName) {
        var platform = StreamingPlatform.builder()
                .name(name)
                .websiteUrl(websiteUrl)
                .logo("https://res.cloudinary.com/demo/image/upload/v1/geektech/streaming-platforms/" + logoName + ".png")
                .build();

        platform.setLogoPublicId("geektech/streaming-platforms/" + logoName);

        return streamingPlatformRepository.saveAndFlush(platform);
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

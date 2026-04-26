package com.diegolima.geekTech;

import com.diegolima.geekTech.models.Anime;
import com.diegolima.geekTech.models.Role;
import com.diegolima.geekTech.models.StreamingPlatform;
import com.diegolima.geekTech.models.User;
import com.diegolima.geekTech.repository.AnimeRepository;
import com.diegolima.geekTech.repository.AnimeStreamingAvailabilityRepository;
import com.diegolima.geekTech.repository.StreamingPlatformRepository;
import com.diegolima.geekTech.repository.UserRepository;
import com.diegolima.geekTech.services.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnimeStreamingAvailabilityControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AnimeStreamingAvailabilityRepository animeStreamingAvailabilityRepository;

    @Autowired
    private AnimeRepository animeRepository;

    @Autowired
    private StreamingPlatformRepository streamingPlatformRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @AfterEach
    void cleanDatabase() {
        animeStreamingAvailabilityRepository.deleteAll();
        streamingPlatformRepository.deleteAll();
        animeRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void linksAnimeToStreamingPlatformWithAudioAndSubtitleLanguages() throws Exception {
        var admin = createAdmin("admin-link-availability@example.com");
        var anime = saveAnime(admin);
        var platform = savePlatform();

        mockMvc.perform(post("/anime/{animeId}/streaming-availabilities", anime.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "streamingPlatformId": %d,
                                  "audioLanguages": ["Japanese", "Portuguese", "Portuguese", " "],
                                  "subtitleLanguages": ["Portuguese", "English"]
                                }
                                """.formatted(platform.getId()))
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.animeId").value(anime.getId()))
                .andExpect(jsonPath("$.animeName").value("Naruto"))
                .andExpect(jsonPath("$.streamingPlatform.id").value(platform.getId()))
                .andExpect(jsonPath("$.streamingPlatform.name").value("Crunchyroll"))
                .andExpect(jsonPath("$.audioLanguages[0]").value("Japanese"))
                .andExpect(jsonPath("$.audioLanguages[1]").value("Portuguese"))
                .andExpect(jsonPath("$.subtitleLanguages[0]").value("Portuguese"))
                .andExpect(jsonPath("$.subtitleLanguages[1]").value("English"));

        var availabilities = animeStreamingAvailabilityRepository.findByAnimeId(anime.getId());

        assertThat(availabilities).hasSize(1);
        assertThat(availabilities.getFirst().getAnime().getId()).isEqualTo(anime.getId());
        assertThat(availabilities.getFirst().getStreamingPlatform().getId()).isEqualTo(platform.getId());
    }

    @Test
    void listsAnimeStreamingAvailabilitiesByAnime() throws Exception {
        var admin = createAdmin("admin-list-availability@example.com");
        var anime = saveAnime(admin);
        var platform = savePlatform();
        var availability = animeStreamingAvailabilityRepository.saveAndFlush(com.diegolima.geekTech.models.AnimeStreamingAvailability.builder()
                .anime(anime)
                .streamingPlatform(platform)
                .audioLanguages(java.util.List.of("Japanese"))
                .subtitleLanguages(java.util.List.of("Portuguese"))
                .build());

        mockMvc.perform(get("/anime/{animeId}/streaming-availabilities", anime.getId())
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(availability.getId()))
                .andExpect(jsonPath("$[0].streamingPlatform.name").value("Crunchyroll"))
                .andExpect(jsonPath("$[0].audioLanguages[0]").value("Japanese"))
                .andExpect(jsonPath("$[0].subtitleLanguages[0]").value("Portuguese"));
    }

    @Test
    void updatesAnimeStreamingAvailabilityLanguages() throws Exception {
        var admin = createAdmin("admin-update-availability@example.com");
        var anime = saveAnime(admin);
        var platform = savePlatform();
        var availability = animeStreamingAvailabilityRepository.saveAndFlush(com.diegolima.geekTech.models.AnimeStreamingAvailability.builder()
                .anime(anime)
                .streamingPlatform(platform)
                .audioLanguages(java.util.List.of("Japanese"))
                .subtitleLanguages(java.util.List.of("Portuguese"))
                .build());

        mockMvc.perform(put("/anime/{animeId}/streaming-availabilities/{availabilityId}", anime.getId(), availability.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "audioLanguages": ["Japanese", "English"],
                                  "subtitleLanguages": ["Portuguese", "Spanish"]
                                }
                                """)
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.audioLanguages[0]").value("Japanese"))
                .andExpect(jsonPath("$.audioLanguages[1]").value("English"))
                .andExpect(jsonPath("$.subtitleLanguages[0]").value("Portuguese"))
                .andExpect(jsonPath("$.subtitleLanguages[1]").value("Spanish"));

        mockMvc.perform(get("/anime/{animeId}/streaming-availabilities/{availabilityId}", anime.getId(), availability.getId())
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.audioLanguages[0]").value("Japanese"))
                .andExpect(jsonPath("$.audioLanguages[1]").value("English"))
                .andExpect(jsonPath("$.subtitleLanguages[0]").value("Portuguese"))
                .andExpect(jsonPath("$.subtitleLanguages[1]").value("Spanish"));
    }

    @Test
    void deletesAnimeStreamingAvailability() throws Exception {
        var admin = createAdmin("admin-delete-availability@example.com");
        var anime = saveAnime(admin);
        var platform = savePlatform();
        var availability = animeStreamingAvailabilityRepository.saveAndFlush(com.diegolima.geekTech.models.AnimeStreamingAvailability.builder()
                .anime(anime)
                .streamingPlatform(platform)
                .audioLanguages(java.util.List.of("Japanese"))
                .subtitleLanguages(java.util.List.of("Portuguese"))
                .build());

        mockMvc.perform(delete("/anime/{animeId}/streaming-availabilities/{availabilityId}", anime.getId(), availability.getId())
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isNoContent());

        assertThat(animeStreamingAvailabilityRepository.findById(availability.getId())).isEmpty();
    }

    @Test
    void returnsBadRequestWhenAnimeIsAlreadyLinkedToPlatform() throws Exception {
        var admin = createAdmin("admin-duplicate-availability@example.com");
        var anime = saveAnime(admin);
        var platform = savePlatform();
        animeStreamingAvailabilityRepository.saveAndFlush(com.diegolima.geekTech.models.AnimeStreamingAvailability.builder()
                .anime(anime)
                .streamingPlatform(platform)
                .audioLanguages(java.util.List.of("Japanese"))
                .subtitleLanguages(java.util.List.of("Portuguese"))
                .build());

        mockMvc.perform(post("/anime/{animeId}/streaming-availabilities", anime.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "streamingPlatformId": %d,
                                  "audioLanguages": ["Japanese"],
                                  "subtitleLanguages": ["Portuguese"]
                                }
                                """.formatted(platform.getId()))
                        .header("Authorization", "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isBadRequest());
    }

    private User createAdmin(String email) {
        return userRepository.save(User.builder()
                .name("Admin User")
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build());
    }

    private Anime saveAnime(User admin) {
        var anime = Anime.builder()
                .name("Naruto")
                .synopsis("A young ninja seeks recognition from his village.")
                .photo("https://res.cloudinary.com/demo/image/upload/v1/geektech/animes/naruto.jpg")
                .createdBy(admin)
                .build();
        anime.setPhotoPublicId("geektech/animes/naruto");
        return animeRepository.saveAndFlush(anime);
    }

    private StreamingPlatform savePlatform() {
        var platform = StreamingPlatform.builder()
                .name("Crunchyroll")
                .websiteUrl("https://www.crunchyroll.com")
                .logo("https://res.cloudinary.com/demo/image/upload/v1/geektech/streaming-platforms/crunchyroll.png")
                .build();
        platform.setLogoPublicId("geektech/streaming-platforms/crunchyroll");
        return streamingPlatformRepository.saveAndFlush(platform);
    }
}

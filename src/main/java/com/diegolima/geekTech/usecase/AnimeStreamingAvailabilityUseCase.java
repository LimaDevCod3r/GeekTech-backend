package com.diegolima.geekTech.usecase;

import com.diegolima.geekTech.dtos.request.CreateAnimeStreamingAvailabilityDTO;
import com.diegolima.geekTech.dtos.request.UpdateAnimeStreamingAvailabilityDTO;
import com.diegolima.geekTech.dtos.response.AnimeStreamingAvailabilityResponseDTO;
import com.diegolima.geekTech.dtos.response.StreamingPlatformResponseDTO;
import com.diegolima.geekTech.models.AnimeStreamingAvailability;
import com.diegolima.geekTech.models.Role;
import com.diegolima.geekTech.repository.AnimeRepository;
import com.diegolima.geekTech.repository.AnimeStreamingAvailabilityRepository;
import com.diegolima.geekTech.repository.StreamingPlatformRepository;
import com.diegolima.geekTech.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnimeStreamingAvailabilityUseCase {

    private final AnimeStreamingAvailabilityRepository animeStreamingAvailabilityRepository;
    private final AnimeRepository animeRepository;
    private final StreamingPlatformRepository streamingPlatformRepository;
    private final UserRepository userRepository;

    public AnimeStreamingAvailabilityUseCase(
            AnimeStreamingAvailabilityRepository animeStreamingAvailabilityRepository,
            AnimeRepository animeRepository,
            StreamingPlatformRepository streamingPlatformRepository,
            UserRepository userRepository
    ) {
        this.animeStreamingAvailabilityRepository = animeStreamingAvailabilityRepository;
        this.animeRepository = animeRepository;
        this.streamingPlatformRepository = streamingPlatformRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AnimeStreamingAvailabilityResponseDTO create(
            Long animeId,
            CreateAnimeStreamingAvailabilityDTO dto,
            Long authenticatedUserId
    ) {
        validateAuthenticatedUserIsAdmin(authenticatedUserId);

        var anime = animeRepository.findById(animeId)
                .orElseThrow(() -> new RuntimeException("Anime not found."));
        var streamingPlatform = streamingPlatformRepository.findById(dto.streamingPlatformId())
                .orElseThrow(() -> new RuntimeException("Streaming platform not found."));

        if (animeStreamingAvailabilityRepository.existsByAnimeIdAndStreamingPlatformId(animeId, dto.streamingPlatformId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Anime is already linked to this streaming platform.");
        }

        var availability = AnimeStreamingAvailability.builder()
                .anime(anime)
                .streamingPlatform(streamingPlatform)
                .audioLanguages(normalizeLanguages(dto.audioLanguages()))
                .subtitleLanguages(normalizeLanguages(dto.subtitleLanguages()))
                .build();

        return toResponse(animeStreamingAvailabilityRepository.saveAndFlush(availability));
    }

    @Transactional(readOnly = true)
    public List<AnimeStreamingAvailabilityResponseDTO> findByAnime(Long animeId) {
        if (!animeRepository.existsById(animeId)) {
            throw new RuntimeException("Anime not found.");
        }

        return animeStreamingAvailabilityRepository.findByAnimeId(animeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AnimeStreamingAvailabilityResponseDTO findById(Long animeId, Long availabilityId) {
        return toResponse(findAvailabilityOrThrow(animeId, availabilityId));
    }

    @Transactional
    public AnimeStreamingAvailabilityResponseDTO update(
            Long animeId,
            Long availabilityId,
            UpdateAnimeStreamingAvailabilityDTO dto,
            Long authenticatedUserId
    ) {
        validateAuthenticatedUserIsAdmin(authenticatedUserId);

        var availability = findAvailabilityOrThrow(animeId, availabilityId);
        availability.setAudioLanguages(normalizeLanguages(dto.audioLanguages()));
        availability.setSubtitleLanguages(normalizeLanguages(dto.subtitleLanguages()));

        return toResponse(animeStreamingAvailabilityRepository.saveAndFlush(availability));
    }

    @Transactional
    public void delete(Long animeId, Long availabilityId, Long authenticatedUserId) {
        validateAuthenticatedUserIsAdmin(authenticatedUserId);

        var availability = findAvailabilityOrThrow(animeId, availabilityId);

        animeStreamingAvailabilityRepository.delete(availability);
        animeStreamingAvailabilityRepository.flush();
    }

    private AnimeStreamingAvailability findAvailabilityOrThrow(Long animeId, Long availabilityId) {
        return animeStreamingAvailabilityRepository.findByIdAndAnimeId(availabilityId, animeId)
                .orElseThrow(() -> new RuntimeException("Anime streaming availability not found."));
    }

    private void validateAuthenticatedUserIsAdmin(Long authenticatedUserId) {
        var authenticatedUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        if (authenticatedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can manage anime streaming availability.");
        }
    }

    private List<String> normalizeLanguages(List<String> languages) {
        if (languages == null) {
            return new ArrayList<>();
        }

        return languages.stream()
                .filter(language -> language != null && !language.isBlank())
                .map(String::trim)
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private AnimeStreamingAvailabilityResponseDTO toResponse(AnimeStreamingAvailability availability) {
        var anime = availability.getAnime();
        var streamingPlatform = availability.getStreamingPlatform();

        return new AnimeStreamingAvailabilityResponseDTO(
                availability.getId(),
                anime.getId(),
                anime.getName(),
                new StreamingPlatformResponseDTO(
                        streamingPlatform.getId(),
                        streamingPlatform.getName(),
                        streamingPlatform.getWebsiteUrl(),
                        streamingPlatform.getLogo()
                ),
                List.copyOf(availability.getAudioLanguages()),
                List.copyOf(availability.getSubtitleLanguages())
        );
    }
}

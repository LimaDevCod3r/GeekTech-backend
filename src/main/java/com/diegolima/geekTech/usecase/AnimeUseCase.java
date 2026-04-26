package com.diegolima.geekTech.usecase;

import com.diegolima.geekTech.dtos.request.CreateAnimeDTO;
import com.diegolima.geekTech.dtos.response.AnimeResponseDTO;
import com.diegolima.geekTech.models.Anime;
import com.diegolima.geekTech.models.Role;
import com.diegolima.geekTech.repository.AnimeRepository;
import com.diegolima.geekTech.repository.UserRepository;
import com.diegolima.geekTech.services.CloudinaryService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnimeUseCase {

    private static final String ANIME_PHOTOS_FOLDER = "geektech/animes";

    private final AnimeRepository animeRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public AnimeUseCase(
            AnimeRepository animeRepository,
            UserRepository userRepository,
            CloudinaryService cloudinaryService
    ) {
        this.animeRepository = animeRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public AnimeResponseDTO create(CreateAnimeDTO dto) {
        var creator = userRepository.findById(dto.createdByUserId())
                .orElseThrow(() -> new IllegalArgumentException("Creator user not found."));

        validateCreatorIsAdmin(creator.getRole());

        var animeName = dto.name().trim();

        validateAnimeDoesNotExist(animeName);

        var uploadedPhoto = cloudinaryService.upload(dto.photo(), ANIME_PHOTOS_FOLDER);

        try {
            var anime = Anime.builder()
                    .name(animeName)
                    .synopsis(dto.synopsis().trim())
                    .photo(uploadedPhoto.secureUrl())
                    .createdBy(creator)
                    .build();

            return toResponse(animeRepository.saveAndFlush(anime));
        } catch (RuntimeException exception) {
            cloudinaryService.delete(uploadedPhoto.publicId());
            throw exception;
        }
    }

    private void validateCreatorIsAdmin(Role role) {
        if (role != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can create anime.");
        }
    }

    private void validateAnimeDoesNotExist(String name) {
        if (animeRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Anime already exists.");
        }
    }

    private AnimeResponseDTO toResponse(Anime anime) {
        return new AnimeResponseDTO(
                anime.getId(),
                anime.getName(),
                anime.getSynopsis(),
                anime.getPhoto()
        );
    }
}
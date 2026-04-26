package com.diegolima.geekTech.usecase;

import com.diegolima.geekTech.dtos.request.CreateAnimeDTO;
import com.diegolima.geekTech.dtos.request.UpdateAnimeDTO;
import com.diegolima.geekTech.dtos.response.AnimeResponseDTO;
import com.diegolima.geekTech.dtos.response.CloudinaryUploadResponse;
import com.diegolima.geekTech.models.Anime;
import com.diegolima.geekTech.models.Role;
import com.diegolima.geekTech.repository.AnimeRepository;
import com.diegolima.geekTech.repository.UserRepository;
import com.diegolima.geekTech.services.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                    .photoPublicId(uploadedPhoto.publicId())
                    .createdBy(creator)
                    .build();

            return toResponse(animeRepository.saveAndFlush(anime));
        } catch (RuntimeException exception) {
            cloudinaryService.delete(uploadedPhoto.publicId());
            throw exception;
        }
    }


    @Transactional(readOnly = true)
    public Page<AnimeResponseDTO> findAll(Pageable pageable) {
        return animeRepository.findAll(pageable).map(this::toResponse);
    }


    @Transactional(readOnly = true)
    public Page<AnimeResponseDTO> findByName(String name, Pageable pageable) {
        if (name == null || name.isBlank()) {
            return findAll(pageable);
        }
        return animeRepository.findByNameContainingIgnoreCase(name.trim(), pageable).map(this::toResponse);
    }


    @Transactional
    public AnimeResponseDTO update(Long id, UpdateAnimeDTO dto, Long authenticatedUserId) {
        var authenticatedUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        validateCreatorIsAdmin(authenticatedUser.getRole());

        var anime = findAnimeOrThrow(id);
        var animeName = dto.name().trim();

        validateAnimeDoesNotExistForAnotherId(animeName, id);

        CloudinaryUploadResponse uploadedPhoto = null;
        var previousPhotoPublicId = anime.getPhotoPublicId();

        if (hasPhoto(dto.photo())) {
            uploadedPhoto = cloudinaryService.upload(dto.photo(), ANIME_PHOTOS_FOLDER);
            anime.setPhoto(uploadedPhoto.secureUrl());
            anime.setPhotoPublicId(uploadedPhoto.publicId());
        }

        anime.setName(animeName);
        anime.setSynopsis(dto.synopsis().trim());

        try {
            var response = toResponse(animeRepository.saveAndFlush(anime));
            deletePhotoIfPresent(previousPhotoPublicId, uploadedPhoto);
            return response;
        } catch (RuntimeException exception) {
            if (uploadedPhoto != null) {
                cloudinaryService.delete(uploadedPhoto.publicId());
            }
            throw exception;
        }
    }


    @Transactional
    public void delete(Long id, Long authenticatedUserId) {
        var authenticatedUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        validateCreatorIsAdmin(authenticatedUser.getRole());

        var anime = findAnimeOrThrow(id);
        var photoPublicId = anime.getPhotoPublicId();

        animeRepository.delete(anime);
        animeRepository.flush();

        deletePhotoIfPresent(photoPublicId);
    }


    public AnimeResponseDTO findByAnime(Long id) {
        return toResponse(findAnimeOrThrow(id));
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

    private void validateAnimeDoesNotExistForAnotherId(String name, Long id) {
        if (animeRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException("Anime already exists.");
        }
    }

    private Anime findAnimeOrThrow(Long id) {
        return animeRepository.findById(id).orElseThrow(() -> new RuntimeException("Anime not found."));
    }

    private boolean hasPhoto(org.springframework.web.multipart.MultipartFile photo) {
        return photo != null && !photo.isEmpty();
    }

    private void deletePhotoIfPresent(String publicId) {
        if (publicId != null && !publicId.isBlank()) {
            cloudinaryService.delete(publicId);
        }
    }

    private void deletePhotoIfPresent(String previousPhotoPublicId, CloudinaryUploadResponse uploadedPhoto) {
        if (uploadedPhoto != null && previousPhotoPublicId != null && !previousPhotoPublicId.isBlank()) {
            cloudinaryService.delete(previousPhotoPublicId);
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

package com.diegolima.geekTech.usecase;

import com.diegolima.geekTech.dtos.request.CreateStreamingPlatformDTO;
import com.diegolima.geekTech.dtos.request.UpdateStreamingPlatformDTO;
import com.diegolima.geekTech.dtos.response.CloudinaryUploadResponse;
import com.diegolima.geekTech.dtos.response.StreamingPlatformResponseDTO;
import com.diegolima.geekTech.models.Role;
import com.diegolima.geekTech.models.StreamingPlatform;
import com.diegolima.geekTech.repository.StreamingPlatformRepository;
import com.diegolima.geekTech.repository.UserRepository;
import com.diegolima.geekTech.services.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StreamingPlatformUseCase {

    private static final String STREAMING_PLATFORM_LOGOS_FOLDER = "geektech/streaming-platforms";

    private final StreamingPlatformRepository streamingPlatformRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public StreamingPlatformUseCase(
            StreamingPlatformRepository streamingPlatformRepository,
            UserRepository userRepository,
            CloudinaryService cloudinaryService
    ) {
        this.streamingPlatformRepository = streamingPlatformRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Transactional
    public StreamingPlatformResponseDTO create(CreateStreamingPlatformDTO dto, Long authenticatedUserId) {
        validateAuthenticatedUserIsAdmin(authenticatedUserId);

        var platformName = dto.name().trim();
        validatePlatformDoesNotExist(platformName);

        var uploadedLogo = cloudinaryService.upload(dto.logo(), STREAMING_PLATFORM_LOGOS_FOLDER);

        try {
            var platform = StreamingPlatform.builder()
                    .name(platformName)
                    .websiteUrl(trimToNull(dto.websiteUrl()))
                    .logo(uploadedLogo.secureUrl())
                    .build();
            platform.setLogoPublicId(uploadedLogo.publicId());

            return toResponse(streamingPlatformRepository.saveAndFlush(platform));
        } catch (RuntimeException exception) {
            cloudinaryService.delete(uploadedLogo.publicId());
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public Page<StreamingPlatformResponseDTO> findAll(Pageable pageable) {
        return streamingPlatformRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<StreamingPlatformResponseDTO> findByName(String name, Pageable pageable) {
        if (name == null || name.isBlank()) {
            return findAll(pageable);
        }
        return streamingPlatformRepository.findByNameContainingIgnoreCase(name.trim(), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public StreamingPlatformResponseDTO findById(Long id) {
        return toResponse(findPlatformOrThrow(id));
    }

    @Transactional
    public StreamingPlatformResponseDTO update(Long id, UpdateStreamingPlatformDTO dto, Long authenticatedUserId) {
        validateAuthenticatedUserIsAdmin(authenticatedUserId);

        var platform = findPlatformOrThrow(id);
        var platformName = dto.name().trim();

        validatePlatformDoesNotExistForAnotherId(platformName, id);

        CloudinaryUploadResponse uploadedLogo = null;
        var previousLogoPublicId = platform.getLogoPublicId();

        if (hasLogo(dto.logo())) {
            uploadedLogo = cloudinaryService.upload(dto.logo(), STREAMING_PLATFORM_LOGOS_FOLDER);
            platform.setLogo(uploadedLogo.secureUrl());
            platform.setLogoPublicId(uploadedLogo.publicId());
        }

        platform.setName(platformName);
        platform.setWebsiteUrl(trimToNull(dto.websiteUrl()));

        try {
            var response = toResponse(streamingPlatformRepository.saveAndFlush(platform));
            deleteLogoIfReplaced(previousLogoPublicId, uploadedLogo);
            return response;
        } catch (RuntimeException exception) {
            if (uploadedLogo != null) {
                cloudinaryService.delete(uploadedLogo.publicId());
            }
            throw exception;
        }
    }

    @Transactional
    public void delete(Long id, Long authenticatedUserId) {
        validateAuthenticatedUserIsAdmin(authenticatedUserId);

        var platform = findPlatformOrThrow(id);
        var logoPublicId = platform.getLogoPublicId();

        streamingPlatformRepository.delete(platform);
        streamingPlatformRepository.flush();

        deleteLogoIfPresent(logoPublicId);
    }

    private void validateAuthenticatedUserIsAdmin(Long authenticatedUserId) {
        var authenticatedUser = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found."));

        if (authenticatedUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can manage streaming platforms.");
        }
    }

    private void validatePlatformDoesNotExist(String name) {
        if (streamingPlatformRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Streaming platform already exists.");
        }
    }

    private void validatePlatformDoesNotExistForAnotherId(String name, Long id) {
        if (streamingPlatformRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new IllegalArgumentException("Streaming platform already exists.");
        }
    }

    private StreamingPlatform findPlatformOrThrow(Long id) {
        return streamingPlatformRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Streaming platform not found."));
    }

    private boolean hasLogo(org.springframework.web.multipart.MultipartFile logo) {
        return logo != null && !logo.isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void deleteLogoIfReplaced(String previousLogoPublicId, CloudinaryUploadResponse uploadedLogo) {
        if (uploadedLogo != null) {
            deleteLogoIfPresent(previousLogoPublicId);
        }
    }

    private void deleteLogoIfPresent(String publicId) {
        if (publicId != null && !publicId.isBlank()) {
            cloudinaryService.delete(publicId);
        }
    }

    private StreamingPlatformResponseDTO toResponse(StreamingPlatform platform) {
        return new StreamingPlatformResponseDTO(
                platform.getId(),
                platform.getName(),
                platform.getWebsiteUrl(),
                platform.getLogo()
        );
    }
}

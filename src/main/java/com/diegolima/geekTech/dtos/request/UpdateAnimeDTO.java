package com.diegolima.geekTech.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "Multipart payload for anime update")
public record UpdateAnimeDTO(
        @Schema(description = "Anime name", example = "Fullmetal Alchemist: Brotherhood")
        @NotBlank(message = "Anime name is required")
        @Size(max = 150, message = "Anime name must have at most 150 characters")
        String name,

        @Schema(description = "Anime synopsis", example = "Two brothers search for the Philosopher's Stone after a failed alchemy ritual.")
        @NotBlank(message = "Synopsis is required")
        @Size(max = 2000, message = "Synopsis must have at most 2000 characters")
        String synopsis,

        @Schema(description = "New anime cover image", type = "string", format = "binary")
        MultipartFile photo
) {
}

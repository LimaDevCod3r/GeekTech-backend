package com.diegolima.geekTech.dtos.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "Multipart payload for anime creation")
public record CreateAnimeDTO(
        @Schema(description = "Anime name", example = "Fullmetal Alchemist: Brotherhood")
        @NotBlank(message = "Anime name is required")
        @Size(max = 150, message = "Anime name must have at most 150 characters")
        String name,


        @Schema(description = "Anime synopsis", example = "Two brothers search for the Philosopher's Stone after a failed alchemy ritual.")
        @NotBlank(message = "Synopsis is required")
        @Size(max = 2000, message = "Synopsis must have at most 2000 characters")
        String synopsis,

        @Schema(description = "Anime cover image", type = "string", format = "binary")
        @NotNull(message = "Photo is required")
        MultipartFile photo,

        @Schema(description = "ID of the ADMIN user creating the anime", example = "1")
        @NotNull
        @JsonProperty("created_by_user_id")
        Long createdByUserId
) {
}

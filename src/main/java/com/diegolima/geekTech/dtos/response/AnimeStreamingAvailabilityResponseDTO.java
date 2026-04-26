package com.diegolima.geekTech.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Anime streaming availability response")
public record AnimeStreamingAvailabilityResponseDTO(
        @Schema(description = "Streaming availability id", example = "1")
        Long id,

        @Schema(description = "Anime id", example = "1")
        Long animeId,

        @Schema(description = "Anime name", example = "Fullmetal Alchemist: Brotherhood")
        String animeName,

        @Schema(description = "Streaming platform")
        StreamingPlatformResponseDTO streamingPlatform,

        @Schema(description = "Available audio languages", example = "[\"Japanese\", \"Portuguese\"]")
        List<String> audioLanguages,

        @Schema(description = "Available subtitle languages", example = "[\"Portuguese\", \"English\"]")
        List<String> subtitleLanguages
) {
}

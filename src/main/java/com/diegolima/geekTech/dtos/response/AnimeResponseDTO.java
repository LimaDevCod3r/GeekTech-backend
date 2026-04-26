package com.diegolima.geekTech.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Anime response")
public record AnimeResponseDTO(
        @Schema(description = "Anime id", example = "1")
        Long id,

        @Schema(description = "Anime name", example = "Fullmetal Alchemist: Brotherhood")
        String name,

        @Schema(description = "Anime synopsis", example = "Two brothers search for the Philosopher's Stone after a failed alchemy ritual.")
        String synopsis,

        @Schema(description = "Cloudinary secure URL for the anime image", example = "https://res.cloudinary.com/demo/image/upload/v1/geektech/animes/fullmetal.jpg")
        String photo
) {
}

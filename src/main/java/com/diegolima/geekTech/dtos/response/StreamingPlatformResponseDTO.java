package com.diegolima.geekTech.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Streaming platform response")
public record StreamingPlatformResponseDTO(
        @Schema(description = "Streaming platform id", example = "1")
        Long id,

        @Schema(description = "Streaming platform name", example = "Crunchyroll")
        String name,

        @Schema(description = "Streaming platform website URL", example = "https://www.crunchyroll.com")
        String websiteUrl,

        @Schema(description = "Cloudinary secure URL for the streaming platform logo", example = "https://res.cloudinary.com/demo/image/upload/v1/geektech/streaming-platforms/crunchyroll.png")
        String logo
) {
}

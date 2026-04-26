package com.diegolima.geekTech.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "Multipart payload for streaming platform update")
public record UpdateStreamingPlatformDTO(
        @Schema(description = "Streaming platform name", example = "Crunchyroll")
        @NotBlank(message = "Streaming platform name is required")
        @Size(max = 120, message = "Streaming platform name must have at most 120 characters")
        String name,

        @Schema(description = "Streaming platform website URL", example = "https://www.crunchyroll.com")
        @Size(max = 500, message = "Website URL must have at most 500 characters")
        String websiteUrl,

        @Schema(description = "New streaming platform logo image", type = "string", format = "binary")
        MultipartFile logo
) {
}

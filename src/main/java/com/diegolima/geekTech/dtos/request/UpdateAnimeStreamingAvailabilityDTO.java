package com.diegolima.geekTech.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Payload to update an anime streaming availability")
public record UpdateAnimeStreamingAvailabilityDTO(
        @Schema(description = "Available audio languages", example = "[\"Japanese\", \"Portuguese\"]")
        @Size(max = 20, message = "Audio languages must have at most 20 items")
        List<String> audioLanguages,

        @Schema(description = "Available subtitle languages", example = "[\"Portuguese\", \"English\"]")
        @Size(max = 20, message = "Subtitle languages must have at most 20 items")
        List<String> subtitleLanguages
) {
}

package com.diegolima.geekTech.controller;

import com.diegolima.geekTech.dtos.request.CreateAnimeStreamingAvailabilityDTO;
import com.diegolima.geekTech.dtos.request.UpdateAnimeStreamingAvailabilityDTO;
import com.diegolima.geekTech.dtos.response.AnimeStreamingAvailabilityResponseDTO;
import com.diegolima.geekTech.models.User;
import com.diegolima.geekTech.usecase.AnimeStreamingAvailabilityUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/anime/{animeId}/streaming-availabilities")
@Tag(name = "Anime Streaming Availability", description = "Links anime to streaming platforms with audio and subtitle languages")
public class AnimeStreamingAvailabilityController {

    private final AnimeStreamingAvailabilityUseCase animeStreamingAvailabilityUseCase;

    public AnimeStreamingAvailabilityController(AnimeStreamingAvailabilityUseCase animeStreamingAvailabilityUseCase) {
        this.animeStreamingAvailabilityUseCase = animeStreamingAvailabilityUseCase;
    }

    @PostMapping
    @Operation(
            summary = "Link an anime to a streaming platform",
            description = "Creates an anime streaming availability with audio and subtitle language lists. Only ADMIN users can create links."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Anime streaming availability created",
                    content = @Content(schema = @Schema(implementation = AnimeStreamingAvailabilityResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or duplicated anime/platform link",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Only admins can create links",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Anime or streaming platform not found",
                    content = @Content)
    })
    public ResponseEntity<AnimeStreamingAvailabilityResponseDTO> create(
            @Parameter(description = "Anime id", example = "1")
            @PathVariable Long animeId,

            @RequestBody CreateAnimeStreamingAvailabilityDTO dto,

            @AuthenticationPrincipal User authenticatedUser
    ) {
        var response = animeStreamingAvailabilityUseCase.create(animeId, dto, getAuthenticatedUserId(authenticatedUser));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "List streaming availability for an anime",
            description = "Returns all streaming platforms linked to an anime with audio and subtitle language lists."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anime streaming availability list returned",
                    content = @Content(schema = @Schema(implementation = AnimeStreamingAvailabilityResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Anime not found",
                    content = @Content)
    })
    public ResponseEntity<List<AnimeStreamingAvailabilityResponseDTO>> findByAnime(
            @Parameter(description = "Anime id", example = "1")
            @PathVariable Long animeId
    ) {
        return ResponseEntity.ok(animeStreamingAvailabilityUseCase.findByAnime(animeId));
    }

    @GetMapping("/{availabilityId}")
    @Operation(
            summary = "Find an anime streaming availability",
            description = "Returns a single anime streaming availability by id."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anime streaming availability found",
                    content = @Content(schema = @Schema(implementation = AnimeStreamingAvailabilityResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Anime streaming availability not found",
                    content = @Content)
    })
    public ResponseEntity<AnimeStreamingAvailabilityResponseDTO> findById(
            @Parameter(description = "Anime id", example = "1")
            @PathVariable Long animeId,

            @Parameter(description = "Anime streaming availability id", example = "1")
            @PathVariable Long availabilityId
    ) {
        return ResponseEntity.ok(animeStreamingAvailabilityUseCase.findById(animeId, availabilityId));
    }

    @PutMapping("/{availabilityId}")
    @Operation(
            summary = "Update anime streaming availability languages",
            description = "Updates audio and subtitle language lists for an anime streaming availability. Only ADMIN users can update links."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anime streaming availability updated",
                    content = @Content(schema = @Schema(implementation = AnimeStreamingAvailabilityResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Only admins can update links",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Anime streaming availability not found",
                    content = @Content)
    })
    public ResponseEntity<AnimeStreamingAvailabilityResponseDTO> update(
            @Parameter(description = "Anime id", example = "1")
            @PathVariable Long animeId,

            @Parameter(description = "Anime streaming availability id", example = "1")
            @PathVariable Long availabilityId,

            @RequestBody UpdateAnimeStreamingAvailabilityDTO dto,

            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.ok(animeStreamingAvailabilityUseCase.update(
                animeId,
                availabilityId,
                dto,
                getAuthenticatedUserId(authenticatedUser)
        ));
    }

    @DeleteMapping("/{availabilityId}")
    @Operation(
            summary = "Delete an anime streaming availability",
            description = "Removes the link between an anime and a streaming platform. Only ADMIN users can delete links."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Anime streaming availability deleted",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Only admins can delete links",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Anime streaming availability not found",
                    content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Anime id", example = "1")
            @PathVariable Long animeId,

            @Parameter(description = "Anime streaming availability id", example = "1")
            @PathVariable Long availabilityId,

            @AuthenticationPrincipal User authenticatedUser
    ) {
        animeStreamingAvailabilityUseCase.delete(animeId, availabilityId, getAuthenticatedUserId(authenticatedUser));
        return ResponseEntity.noContent().build();
    }

    private Long getAuthenticatedUserId(User authenticatedUser) {
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return authenticatedUser.getId();
    }
}

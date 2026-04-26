package com.diegolima.geekTech.controller;

import com.diegolima.geekTech.dtos.request.CreateAnimeDTO;
import com.diegolima.geekTech.dtos.request.UpdateAnimeDTO;
import com.diegolima.geekTech.dtos.response.AnimeResponseDTO;
import com.diegolima.geekTech.dtos.response.PageResponseDTO;
import com.diegolima.geekTech.models.User;
import com.diegolima.geekTech.usecase.AnimeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/anime")
@Tag(name = "Anime", description = "Anime catalog management endpoints")
public class AnimeController {
    private final AnimeUseCase animeUseCase;

    public AnimeController(AnimeUseCase animeUseCase) {
        this.animeUseCase = animeUseCase;
    }

    @GetMapping
    @Operation(
            summary = "List anime",
            description = "Returns a paginated list of anime from the catalog."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anime page returned",
                    content = @Content(schema = @Schema(implementation = AnimeResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content)
    })
    public ResponseEntity<PageResponseDTO<AnimeResponseDTO>> findAllAnimes(
            @ParameterObject
            @PageableDefault(size = 10, sort = "name")
            Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponseDTO.from(animeUseCase.findAll(pageable)));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search anime by name",
            description = "Returns a paginated list of anime whose names contain the provided search term."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anime page returned",
                    content = @Content(schema = @Schema(implementation = AnimeResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content)
    })
    public ResponseEntity<PageResponseDTO<AnimeResponseDTO>> findAnimesByName(
            @Parameter(description = "Anime name search term", example = "naruto")
            @RequestParam String name,

            @ParameterObject
            @PageableDefault(size = 10, sort = "name")
            Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponseDTO.from(animeUseCase.findByName(name, pageable)));
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create an anime",
            description = "Creates an anime and uploads its cover image to Cloudinary. Only ADMIN users can create anime."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Anime created",
                    content = @Content(schema = @Schema(implementation = AnimeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request, duplicated anime, or invalid image",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Only admins can create anime",
                    content = @Content)
    })
    public ResponseEntity<AnimeResponseDTO> create(
            @Parameter(description = "Anime name", example = "Fullmetal Alchemist: Brotherhood")
            @RequestParam String name,

            @Parameter(description = "Anime synopsis", example = "Two brothers search for the Philosopher's Stone after a failed alchemy ritual.")
            @RequestParam String synopsis,

            @Parameter(description = "Anime cover image file")
            @RequestParam MultipartFile photo,

            @Parameter(description = "ID of the ADMIN user creating the anime", example = "1")
            @RequestParam("created_by_user_id") Long createdByUserId
    ) {
        var createAnimeDTO = new CreateAnimeDTO(name, synopsis, photo, createdByUserId);
        var response = animeUseCase.create(createAnimeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Find an anime by id",
            description = "Returns a single anime from the catalog by its identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anime found",
                    content = @Content(schema = @Schema(implementation = AnimeResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Anime not found",
                    content = @Content)
    })
    public ResponseEntity<AnimeResponseDTO> findByAnime(
            @Parameter(description = "Anime id", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok().body(animeUseCase.findByAnime(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update an anime",
            description = "Updates anime data. When a new cover image is sent, uploads the new image and removes the previous Cloudinary upload. Only ADMIN users can update anime."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Anime updated",
                    content = @Content(schema = @Schema(implementation = AnimeResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request, duplicated anime, or invalid image",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Only admins can update anime",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Anime not found",
                    content = @Content)
    })
    public ResponseEntity<AnimeResponseDTO> update(
            @Parameter(description = "Anime id", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Anime name", example = "Fullmetal Alchemist: Brotherhood")
            @RequestParam String name,

            @Parameter(description = "Anime synopsis", example = "Two brothers search for the Philosopher's Stone after a failed alchemy ritual.")
            @RequestParam String synopsis,

            @Parameter(description = "New anime cover image file")
            @RequestParam(required = false) MultipartFile photo,

            @AuthenticationPrincipal User authenticatedUser
    ) {
        var updateAnimeDTO = new UpdateAnimeDTO(name, synopsis, photo);
        return ResponseEntity.ok(animeUseCase.update(id, updateAnimeDTO, authenticatedUser.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an anime",
            description = "Deletes an anime and removes its cover image from Cloudinary. Only ADMIN users can delete anime."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Anime deleted",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Only admins can delete anime",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Anime not found",
                    content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Anime id", example = "1")
            @PathVariable Long id,

            @AuthenticationPrincipal User authenticatedUser
    ) {
        animeUseCase.delete(id, authenticatedUser.getId());
        return ResponseEntity.noContent().build();
    }
}

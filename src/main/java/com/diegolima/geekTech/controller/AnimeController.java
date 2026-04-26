package com.diegolima.geekTech.controller;

import com.diegolima.geekTech.dtos.request.CreateAnimeDTO;
import com.diegolima.geekTech.dtos.response.AnimeResponseDTO;
import com.diegolima.geekTech.usecase.AnimeUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/anime")
@Tag(name = "Anime", description = "Anime catalog management endpoints")
public class AnimeController {
    private final AnimeUseCase animeUseCase;

    public AnimeController(AnimeUseCase animeUseCase) {
        this.animeUseCase = animeUseCase;
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
}

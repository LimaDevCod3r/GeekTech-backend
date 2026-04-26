package com.diegolima.geekTech.controller;

import com.diegolima.geekTech.dtos.request.CreateStreamingPlatformDTO;
import com.diegolima.geekTech.dtos.request.UpdateStreamingPlatformDTO;
import com.diegolima.geekTech.dtos.response.PageResponseDTO;
import com.diegolima.geekTech.dtos.response.StreamingPlatformResponseDTO;
import com.diegolima.geekTech.models.User;
import com.diegolima.geekTech.usecase.StreamingPlatformUseCase;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/platform")
@Tag(name = "Streaming Platform", description = "Streaming platform management endpoints")
public class StreamingPlatformController {

    private final StreamingPlatformUseCase streamingPlatformUseCase;

    public StreamingPlatformController(StreamingPlatformUseCase streamingPlatformUseCase) {
        this.streamingPlatformUseCase = streamingPlatformUseCase;
    }

    @GetMapping
    @Operation(
            summary = "List streaming platforms",
            description = "Returns a paginated list of streaming platforms."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Streaming platform page returned",
                    content = @Content(schema = @Schema(implementation = StreamingPlatformResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content)
    })
    public ResponseEntity<PageResponseDTO<StreamingPlatformResponseDTO>> findAll(
            @ParameterObject
            @PageableDefault(size = 10, sort = "name")
            Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponseDTO.from(streamingPlatformUseCase.findAll(pageable)));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search streaming platforms by name",
            description = "Returns a paginated list of streaming platforms whose names contain the provided search term."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Streaming platform page returned",
                    content = @Content(schema = @Schema(implementation = StreamingPlatformResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content)
    })
    public ResponseEntity<PageResponseDTO<StreamingPlatformResponseDTO>> findByName(
            @Parameter(description = "Streaming platform name search term", example = "crunchy")
            @RequestParam String name,

            @ParameterObject
            @PageableDefault(size = 10, sort = "name")
            Pageable pageable
    ) {
        return ResponseEntity.ok(PageResponseDTO.from(streamingPlatformUseCase.findByName(name, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Find a streaming platform by id",
            description = "Returns a single streaming platform by its identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Streaming platform found",
                    content = @Content(schema = @Schema(implementation = StreamingPlatformResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Streaming platform not found",
                    content = @Content)
    })
    public ResponseEntity<StreamingPlatformResponseDTO> findById(
            @Parameter(description = "Streaming platform id", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(streamingPlatformUseCase.findById(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Create a streaming platform",
            description = "Creates a streaming platform and uploads its logo to Cloudinary. Only ADMIN users can create streaming platforms."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Streaming platform created",
                    content = @Content(schema = @Schema(implementation = StreamingPlatformResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request, duplicated streaming platform, or invalid image",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Only admins can create streaming platforms",
                    content = @Content)
    })
    public ResponseEntity<StreamingPlatformResponseDTO> create(
            @Parameter(description = "Streaming platform name", example = "Crunchyroll")
            @RequestParam String name,

            @Parameter(description = "Streaming platform website URL", example = "https://www.crunchyroll.com")
            @RequestParam(required = false) String websiteUrl,

            @Parameter(description = "Streaming platform logo image file")
            @RequestParam MultipartFile logo,

            @AuthenticationPrincipal User authenticatedUser
    ) {
        var createStreamingPlatformDTO = new CreateStreamingPlatformDTO(name, websiteUrl, logo);
        var response = streamingPlatformUseCase.create(createStreamingPlatformDTO, getAuthenticatedUserId(authenticatedUser));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Update a streaming platform",
            description = "Updates streaming platform data. When a new logo is sent, uploads the new image and removes the previous Cloudinary upload. Only ADMIN users can update streaming platforms."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Streaming platform updated",
                    content = @Content(schema = @Schema(implementation = StreamingPlatformResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request, duplicated streaming platform, or invalid image",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Only admins can update streaming platforms",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Streaming platform not found",
                    content = @Content)
    })
    public ResponseEntity<StreamingPlatformResponseDTO> update(
            @Parameter(description = "Streaming platform id", example = "1")
            @PathVariable Long id,

            @Parameter(description = "Streaming platform name", example = "Crunchyroll")
            @RequestParam String name,

            @Parameter(description = "Streaming platform website URL", example = "https://www.crunchyroll.com")
            @RequestParam(required = false) String websiteUrl,

            @Parameter(description = "New streaming platform logo image file")
            @RequestParam(required = false) MultipartFile logo,

            @AuthenticationPrincipal User authenticatedUser
    ) {
        var updateStreamingPlatformDTO = new UpdateStreamingPlatformDTO(name, websiteUrl, logo);
        return ResponseEntity.ok(streamingPlatformUseCase.update(id, updateStreamingPlatformDTO, getAuthenticatedUserId(authenticatedUser)));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a streaming platform",
            description = "Deletes a streaming platform and removes its logo from Cloudinary. Only ADMIN users can delete streaming platforms."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Streaming platform deleted",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication is required",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Only admins can delete streaming platforms",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Streaming platform not found",
                    content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Streaming platform id", example = "1")
            @PathVariable Long id,

            @AuthenticationPrincipal User authenticatedUser
    ) {
        streamingPlatformUseCase.delete(id, getAuthenticatedUserId(authenticatedUser));
        return ResponseEntity.noContent().build();
    }

    private Long getAuthenticatedUserId(User authenticatedUser) {
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }
        return authenticatedUser.getId();
    }
}

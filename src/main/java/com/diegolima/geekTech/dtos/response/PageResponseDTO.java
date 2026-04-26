package com.diegolima.geekTech.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Paginated response")
public record PageResponseDTO<T>(
        @Schema(description = "Current page content")
        List<T> content,

        @Schema(description = "Current page number", example = "0")
        int page,

        @Schema(description = "Current page size", example = "10")
        int size,

        @Schema(description = "Total elements", example = "25")
        long totalElements,

        @Schema(description = "Total pages", example = "3")
        int totalPages,

        @Schema(description = "Whether this is the first page", example = "true")
        boolean first,

        @Schema(description = "Whether this is the last page", example = "false")
        boolean last
) {
    public static <T> PageResponseDTO<T> from(Page<T> page) {
        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}

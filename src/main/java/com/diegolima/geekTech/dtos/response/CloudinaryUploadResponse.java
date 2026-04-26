package com.diegolima.geekTech.dtos.response;

public record CloudinaryUploadResponse(
        String publicId,
        String secureUrl
) {
}
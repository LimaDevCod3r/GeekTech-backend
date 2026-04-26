package com.diegolima.geekTech.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.diegolima.geekTech.dtos.response.CloudinaryUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;


@Service
public class CloudinaryService {

    private static final String RESOURCE_TYPE = "image";

    private final Cloudinary cloudinary;

    public CloudinaryService(@Value("${cloudinary.url}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    // 6. Método público responsável por fazer upload de uma imagem.
    public CloudinaryUploadResponse upload(MultipartFile file, String folder) {
        validateFile(file);
        validateFolder(folder);
        try {
            Map<String, Object> options = buildUploadOptions(folder);
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            return buildUploadResponse(uploadResult);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to upload file to Cloudinary.", exception);
        }
    }


    public void delete(String publicId) {
        validatePublicId(publicId);
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to delete file from Cloudinary.", exception);
        }
    }


    private Map<String, Object> buildUploadOptions(String folder) {
        return ObjectUtils.asMap("folder", folder, "resource_type", RESOURCE_TYPE);
    }


    private CloudinaryUploadResponse buildUploadResponse(Map<?, ?> uploadResult) {
        String publicId = String.valueOf(uploadResult.get("public_id"));
        String secureUrl = String.valueOf(uploadResult.get("secure_url"));
        return new CloudinaryUploadResponse(publicId, secureUrl);
    }


    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image uploads are allowed.");
        }
    }


    private void validateFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            throw new IllegalArgumentException("Cloudinary folder is required.");
        }
    }


    private void validatePublicId(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("Cloudinary public id is required.");
        }
    }
}
package com.badminton.service.impl;

import com.badminton.exception.CustomException;
import com.badminton.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg") && !contentType.equals("image/jpg") && !contentType.equals("image/webp"))) {
            throw new CustomException("Only PNG/JPG/WEBP files are accepted", HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new CustomException("File size exceeds the 10MB limit", HttpStatus.BAD_REQUEST);
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            if (uploadResult == null || uploadResult.get("secure_url") == null) {
                throw new IOException("Failed to obtain secure_url from Cloudinary");
            }
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new CustomException("Cloud storage service is temporarily unavailable. Please try again later.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}

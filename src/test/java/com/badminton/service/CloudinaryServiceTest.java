package com.badminton.service;

import com.badminton.exception.CustomException;
import com.badminton.service.impl.CloudinaryServiceImpl;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private com.cloudinary.Uploader uploader;

    @InjectMocks
    private CloudinaryServiceImpl cloudinaryService;

    @Test
    void uploadFile_success_png() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test-image-content".getBytes()
        );

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://example.com/image.png");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadFile(file);

        assertNotNull(result);
        assertEquals("https://example.com/image.png", result);
        verify(uploader).upload(any(byte[].class), anyMap());
    }

    @Test
    void uploadFile_success_jpeg() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test-image-content".getBytes()
        );

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://example.com/image.jpg");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadFile(file);

        assertNotNull(result);
        assertEquals("https://example.com/image.jpg", result);
    }

    @Test
    void uploadFile_success_webp() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.webp",
                "image/webp",
                "test-image-content".getBytes()
        );

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", "https://example.com/image.webp");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        String result = cloudinaryService.uploadFile(file);

        assertNotNull(result);
        assertEquals("https://example.com/image.webp", result);
    }

    @Test
    void uploadFile_invalidContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test-pdf-content".getBytes()
        );

        CustomException exception = assertThrows(CustomException.class, () -> cloudinaryService.uploadFile(file));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Only PNG/JPG/WEBP files are accepted", exception.getMessage());
        verify(cloudinary, never()).uploader();
    }

    @Test
    void uploadFile_nullContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                null,
                "test-content".getBytes()
        );

        CustomException exception = assertThrows(CustomException.class, () -> cloudinaryService.uploadFile(file));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Only PNG/JPG/WEBP files are accepted", exception.getMessage());
        verify(cloudinary, never()).uploader();
    }

    @Test
    void uploadFile_fileSizeExceedsLimit() {
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                largeContent
        );

        CustomException exception = assertThrows(CustomException.class, () -> cloudinaryService.uploadFile(file));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("File size exceeds the 10MB limit", exception.getMessage());
        verify(cloudinary, never()).uploader();
    }

    @Test
    void uploadFile_cloudinaryFailure() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test-image-content".getBytes()
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("Cloudinary error"));

        CustomException exception = assertThrows(CustomException.class, () -> cloudinaryService.uploadFile(file));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
        assertEquals("Cloud storage service is temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    void uploadFile_nullUploadResult() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test-image-content".getBytes()
        );

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () -> cloudinaryService.uploadFile(file));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
        assertEquals("Cloud storage service is temporarily unavailable. Please try again later.", exception.getMessage());
    }

    @Test
    void uploadFile_nullSecureUrl() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test-image-content".getBytes()
        );

        Map<String, Object> uploadResult = new HashMap<>();
        uploadResult.put("secure_url", null);

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(uploadResult);

        CustomException exception = assertThrows(CustomException.class, () -> cloudinaryService.uploadFile(file));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatus());
        assertEquals("Cloud storage service is temporarily unavailable. Please try again later.", exception.getMessage());
    }
}

package com.badminton.controller;

import com.badminton.dto.CourtDTO;
import com.badminton.dto.response.ResponseDTO;
import com.badminton.entity.Court;
import com.badminton.entity.CourtImage;
import com.badminton.exception.CustomException;
import com.badminton.repository.CourtImageRepository;
import com.badminton.repository.CourtRepository;
import com.badminton.service.CloudinaryService;
import com.badminton.service.CourtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manager/courts")
public class ManagerCourtController {

    private final CloudinaryService cloudinaryService;
    private final CourtRepository courtRepository;
    private final CourtImageRepository courtImageRepository;
    private final CourtService courtService;

    public ManagerCourtController(CloudinaryService cloudinaryService,
                                  CourtRepository courtRepository,
                                  CourtImageRepository courtImageRepository,
                                  CourtService courtService) {
        this.cloudinaryService = cloudinaryService;
        this.courtRepository = courtRepository;
        this.courtImageRepository = courtImageRepository;
        this.courtService = courtService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<CourtDTO>>> getAllCourts() {
        List<CourtDTO> courts = courtService.getAllCourts();
        ResponseDTO<List<CourtDTO>> response = ResponseDTO.<List<CourtDTO>>builder()
                .success(true)
                .message("Fetched all courts successfully")
                .data(courts)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<CourtDTO>> getCourtById(@PathVariable Long id) {
        CourtDTO court = courtService.getCourtById(id);
        ResponseDTO<CourtDTO> response = ResponseDTO.<CourtDTO>builder()
                .success(true)
                .message("Fetched court successfully")
                .data(court)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<CourtDTO>> createCourt(@Valid @RequestBody CourtDTO courtDTO) {
        CourtDTO createdCourt = courtService.createCourt(courtDTO);
        ResponseDTO<CourtDTO> response = ResponseDTO.<CourtDTO>builder()
                .success(true)
                .message("Court created successfully")
                .data(createdCourt)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<CourtDTO>> updateCourt(
            @PathVariable Long id,
            @Valid @RequestBody CourtDTO courtDTO) {
        CourtDTO updatedCourt = courtService.updateCourt(id, courtDTO);
        ResponseDTO<CourtDTO> response = ResponseDTO.<CourtDTO>builder()
                .success(true)
                .message("Court updated successfully")
                .data(updatedCourt)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteCourt(@PathVariable Long id) {
        courtService.deleteCourt(id);
        ResponseDTO<Void> response = ResponseDTO.<Void>builder()
                .success(true)
                .message("Court deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{courtId}/images")
    public ResponseEntity<ResponseDTO<String>> uploadCourtImage(
            @PathVariable Long courtId,
            @RequestParam("file") MultipartFile file) {

        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new CustomException("Court not found", HttpStatus.NOT_FOUND));

        String imageUrl = cloudinaryService.uploadFile(file);

        CourtImage courtImage = CourtImage.builder()
                .court(court)
                .imageUrl(imageUrl)
                .build();
        courtImageRepository.save(courtImage);

        ResponseDTO<String> response = ResponseDTO.<String>builder()
                .success(true)
                .message("Image uploaded successfully")
                .data(imageUrl)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{courtId}/images")
    public ResponseEntity<ResponseDTO<List<String>>> getCourtImages(@PathVariable Long courtId) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new CustomException("Court not found", HttpStatus.NOT_FOUND));

        List<String> imageUrls = courtImageRepository.findByCourtId(courtId)
                .stream()
                .map(CourtImage::getImageUrl)
                .toList();

        ResponseDTO<List<String>> response = ResponseDTO.<List<String>>builder()
                .success(true)
                .message("Fetched court images successfully")
                .data(imageUrls)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{courtId}/images/{imageId}")
    public ResponseEntity<ResponseDTO<Void>> deleteCourtImage(
            @PathVariable Long courtId,
            @PathVariable Long imageId) {

        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new CustomException("Court not found", HttpStatus.NOT_FOUND));

        CourtImage courtImage = courtImageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException("Image not found", HttpStatus.NOT_FOUND));

        if (!courtImage.getCourt().getId().equals(courtId)) {
            throw new CustomException("Image does not belong to this court", HttpStatus.BAD_REQUEST);
        }

        courtImageRepository.deleteById(imageId);

        ResponseDTO<Void> response = ResponseDTO.<Void>builder()
                .success(true)
                .message("Image deleted successfully")
                .build();

        return ResponseEntity.ok(response);
    }
}

package com.badminton.controller;

import com.badminton.dto.CourtDTO;
import com.badminton.entity.Court;
import com.badminton.entity.CourtImage;
import com.badminton.exception.CustomException;
import com.badminton.repository.CourtImageRepository;
import com.badminton.repository.CourtRepository;
import com.badminton.service.CloudinaryService;
import com.badminton.service.CourtService;
import com.badminton.service.RedisTokenBlacklistService;
import com.badminton.config.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.badminton.config.SecurityConfig;
import com.badminton.config.JwtRequestFilter;

@WebMvcTest(ManagerCourtController.class)
@Import({SecurityConfig.class, JwtRequestFilter.class})
class ManagerCourtControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CloudinaryService cloudinaryService;

    @MockBean
    private CourtRepository courtRepository;

    @MockBean
    private CourtImageRepository courtImageRepository;

    @MockBean
    private CourtService courtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private RedisTokenBlacklistService redisTokenBlacklistService;

    @Test
    @WithMockUser(username = "manager@gmail.com", roles = "MANAGER")
    void getAllCourts_success() throws Exception {
        CourtDTO courtDTO = CourtDTO.builder()
                .id(1L)
                .name("Court A")
                .address("123 Street")
                .build();

        when(courtService.getAllCourts()).thenReturn(List.of(courtDTO));

        mockMvc.perform(get("/api/v1/manager/courts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Court A"));
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", roles = "MANAGER")
    void getCourtById_success() throws Exception {
        CourtDTO courtDTO = CourtDTO.builder()
                .id(1L)
                .name("Court A")
                .address("123 Street")
                .build();

        when(courtService.getCourtById(1L)).thenReturn(courtDTO);

        mockMvc.perform(get("/api/v1/manager/courts/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Court A"));
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", roles = "MANAGER")
    void createCourt_success() throws Exception {
        CourtDTO courtDTO = CourtDTO.builder()
                .name("Court A")
                .address("123 Street")
                .pricePerHour(50.0)
                .managerId(2L)
                .build();

        CourtDTO createdCourt = CourtDTO.builder()
                .id(1L)
                .name("Court A")
                .address("123 Street")
                .pricePerHour(50.0)
                .managerId(2L)
                .build();

        when(courtService.createCourt(any(CourtDTO.class))).thenReturn(createdCourt);

        mockMvc.perform(post("/api/v1/manager/courts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courtDTO))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("Court A"));
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", roles = "MANAGER")
    void updateCourt_success() throws Exception {
        CourtDTO courtDTO = CourtDTO.builder()
                .name("Court A Updated")
                .address("456 Street")
                .pricePerHour(60.0)
                .managerId(2L)
                .build();

        CourtDTO updatedCourt = CourtDTO.builder()
                .id(1L)
                .name("Court A Updated")
                .address("456 Street")
                .pricePerHour(60.0)
                .managerId(2L)
                .build();

        when(courtService.updateCourt(eq(1L), any(CourtDTO.class))).thenReturn(updatedCourt);

        mockMvc.perform(put("/api/v1/manager/courts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courtDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Court A Updated"));
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", roles = "MANAGER")
    void deleteCourt_success() throws Exception {
        doNothing().when(courtService).deleteCourt(1L);

        mockMvc.perform(delete("/api/v1/manager/courts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Court deleted successfully"));
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", roles = "MANAGER")
    void uploadCourtImage_success() throws Exception {
        Court court = Court.builder().id(1L).name("Court A").build();
        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(cloudinaryService.uploadFile(any(MultipartFile.class))).thenReturn("https://example.com/image.jpg");

        mockMvc.perform(multipart("/api/v1/manager/courts/1/images")
                        .file("file", "test-image-content".getBytes())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Image uploaded successfully"))
                .andExpect(jsonPath("$.data").value("https://example.com/image.jpg"));
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", roles = "MANAGER")
    void getCourtImages_success() throws Exception {
        Court court = Court.builder().id(1L).name("Court A").build();
        CourtImage courtImage = CourtImage.builder()
                .id(1L)
                .court(court)
                .imageUrl("https://example.com/image.jpg")
                .build();

        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(courtImageRepository.findByCourtId(1L)).thenReturn(List.of(courtImage));

        mockMvc.perform(get("/api/v1/manager/courts/1/images")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0]").value("https://example.com/image.jpg"));
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", roles = "MANAGER")
    void deleteCourtImage_success() throws Exception {
        Court court = Court.builder().id(1L).name("Court A").build();
        CourtImage courtImage = CourtImage.builder()
                .id(1L)
                .court(court)
                .imageUrl("https://example.com/image.jpg")
                .build();

        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(courtImageRepository.findById(1L)).thenReturn(Optional.of(courtImage));
        doNothing().when(courtImageRepository).deleteById(1L);

        mockMvc.perform(delete("/api/v1/manager/courts/1/images/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Image deleted successfully"));
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", roles = "MANAGER")
    void uploadCourtImage_courtNotFound() throws Exception {
        when(courtRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(multipart("/api/v1/manager/courts/999/images")
                        .file("file", "test-image-content".getBytes())
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Court not found"));
    }
}

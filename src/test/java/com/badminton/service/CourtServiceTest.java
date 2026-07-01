package com.badminton.service;

import com.badminton.dto.CourtDTO;
import com.badminton.entity.Court;
import com.badminton.entity.User;
import com.badminton.exception.CustomException;
import com.badminton.repository.CourtRepository;
import com.badminton.repository.UserRepository;
import com.badminton.service.impl.CourtServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourtServiceTest {

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourtServiceImpl courtService;

    @Test
    void getAllCourts_success() {
        Court court = Court.builder()
                .id(1L)
                .name("Court A")
                .address("123 Street")
                .pricePerHour(50.0)
                .manager(User.builder().id(2L).build())
                .createdAt(LocalDateTime.now())
                .build();

        when(courtRepository.findAll()).thenReturn(List.of(court));

        List<CourtDTO> result = courtService.getAllCourts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Court A", result.get(0).getName());
    }

    @Test
    void getCourtById_success() {
        Court court = Court.builder()
                .id(1L)
                .name("Court A")
                .address("123 Street")
                .pricePerHour(50.0)
                .manager(User.builder().id(2L).build())
                .createdAt(LocalDateTime.now())
                .build();

        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));

        CourtDTO result = courtService.getCourtById(1L);

        assertNotNull(result);
        assertEquals("Court A", result.getName());
        assertEquals(1L, result.getId());
    }

    @Test
    void getCourtById_notFound() {
        when(courtRepository.findById(999L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> courtService.getCourtById(999L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Court not found with id: 999", exception.getMessage());
    }

    @Test
    void createCourt_success() {
        CourtDTO courtDTO = CourtDTO.builder()
                .name("Court A")
                .address("123 Street")
                .pricePerHour(50.0)
                .managerId(2L)
                .build();

        User manager = User.builder()
                .id(2L)
                .username("manager")
                .email("manager@test.com")
                .role("MANAGER")
                .build();

        Court court = Court.builder()
                .id(1L)
                .name("Court A")
                .address("123 Street")
                .pricePerHour(50.0)
                .manager(manager)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(courtRepository.save(any(Court.class))).thenReturn(court);

        CourtDTO result = courtService.createCourt(courtDTO);

        assertNotNull(result);
        assertEquals("Court A", result.getName());
        assertEquals(1L, result.getId());
        assertEquals(2L, result.getManagerId());
    }

    @Test
    void createCourt_managerNotFound() {
        CourtDTO courtDTO = CourtDTO.builder()
                .name("Court A")
                .address("123 Street")
                .pricePerHour(50.0)
                .managerId(999L)
                .build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> courtService.createCourt(courtDTO));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Manager not found with id: 999", exception.getMessage());
    }

    @Test
    void updateCourt_success() {
        CourtDTO courtDTO = CourtDTO.builder()
                .name("Court A Updated")
                .address("456 Street")
                .pricePerHour(60.0)
                .managerId(2L)
                .build();

        User manager = User.builder()
                .id(2L)
                .username("manager")
                .email("manager@test.com")
                .role("MANAGER")
                .build();

        Court existingCourt = Court.builder()
                .id(1L)
                .name("Court A")
                .address("123 Street")
                .pricePerHour(50.0)
                .manager(manager)
                .createdAt(LocalDateTime.now())
                .build();

        Court updatedCourt = Court.builder()
                .id(1L)
                .name("Court A Updated")
                .address("456 Street")
                .pricePerHour(60.0)
                .manager(manager)
                .createdAt(LocalDateTime.now())
                .build();

        when(courtRepository.findById(1L)).thenReturn(Optional.of(existingCourt));
        when(userRepository.findById(2L)).thenReturn(Optional.of(manager));
        when(courtRepository.save(any(Court.class))).thenReturn(updatedCourt);

        CourtDTO result = courtService.updateCourt(1L, courtDTO);

        assertNotNull(result);
        assertEquals("Court A Updated", result.getName());
        assertEquals("456 Street", result.getAddress());
        assertEquals(60.0, result.getPricePerHour());
    }

    @Test
    void updateCourt_notFound() {
        CourtDTO courtDTO = CourtDTO.builder()
                .name("Court A")
                .build();

        when(courtRepository.findById(999L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> courtService.updateCourt(999L, courtDTO));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Court not found with id: 999", exception.getMessage());
    }

    @Test
    void updateCourt_managerNotFound() {
        CourtDTO courtDTO = CourtDTO.builder()
                .name("Court A")
                .managerId(999L)
                .build();

        Court existingCourt = Court.builder()
                .id(1L)
                .name("Court A")
                .address("123 Street")
                .pricePerHour(50.0)
                .manager(User.builder().id(2L).build())
                .createdAt(LocalDateTime.now())
                .build();

        when(courtRepository.findById(1L)).thenReturn(Optional.of(existingCourt));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> courtService.updateCourt(1L, courtDTO));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Manager not found with id: 999", exception.getMessage());
    }

    @Test
    void deleteCourt_success() {
        when(courtRepository.existsById(1L)).thenReturn(true);
        doNothing().when(courtRepository).deleteById(1L);

        courtService.deleteCourt(1L);

        verify(courtRepository).deleteById(1L);
    }

    @Test
    void deleteCourt_notFound() {
        when(courtRepository.existsById(999L)).thenReturn(false);

        CustomException exception = assertThrows(CustomException.class, () -> courtService.deleteCourt(999L));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Court not found with id: 999", exception.getMessage());
    }
}

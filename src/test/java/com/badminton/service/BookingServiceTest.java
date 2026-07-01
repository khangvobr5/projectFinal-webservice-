package com.badminton.service;

import com.badminton.dto.BookingDTO;
import com.badminton.dto.request.BookingRequest;
import com.badminton.entity.Booking;
import com.badminton.entity.Court;
import com.badminton.entity.TimeSlot;
import com.badminton.entity.User;
import com.badminton.exception.CustomException;
import com.badminton.repository.BookingRepository;
import com.badminton.repository.CourtRepository;
import com.badminton.repository.TimeSlotRepository;
import com.badminton.repository.UserRepository;
import com.badminton.service.impl.BookingServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("customer@gmail.com");
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createBooking_success() {
        
        BookingRequest request = new BookingRequest(1L, LocalDate.of(2026, 7, 10), 2L);

        Court court = Court.builder().id(1L).name("Court A").build();
        TimeSlot timeSlot = TimeSlot.builder().id(2L).label("08:00 - 09:00").build();
        User customer = User.builder().id(3L).email("customer@gmail.com").build();

        Booking savedBooking = Booking.builder()
                .id(100L)
                .customer(customer)
                .court(court)
                .timeSlot(timeSlot)
                .bookingDate(request.getBookingDate())
                .status("PENDING")
                .build();

        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(timeSlotRepository.findById(2L)).thenReturn(Optional.of(timeSlot));
        when(bookingRepository.existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusIn(
                eq(1L), eq(request.getBookingDate()), eq(2L), anyList()))
                .thenReturn(false);
        when(userRepository.findByEmail("customer@gmail.com")).thenReturn(Optional.of(customer));
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        
        BookingDTO result = bookingService.createBooking(request);

        
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("PENDING", result.getStatus());
        assertEquals(1L, result.getCourtId());
        assertEquals(2L, result.getTimeSlotId());
        assertEquals(3L, result.getCustomerId());

        verify(courtRepository).findById(1L);
        verify(timeSlotRepository).findById(2L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_conflict() {
        
        BookingRequest request = new BookingRequest(1L, LocalDate.of(2026, 7, 10), 2L);

        Court court = Court.builder().id(1L).name("Court A").build();
        TimeSlot timeSlot = TimeSlot.builder().id(2L).label("08:00 - 09:00").build();

        when(courtRepository.findById(1L)).thenReturn(Optional.of(court));
        when(timeSlotRepository.findById(2L)).thenReturn(Optional.of(timeSlot));
        when(bookingRepository.existsByCourtIdAndBookingDateAndTimeSlotIdAndStatusIn(
                eq(1L), eq(request.getBookingDate()), eq(2L), anyList()))
                .thenReturn(true);

        
        CustomException exception = assertThrows(CustomException.class, () -> bookingService.createBooking(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("This time slot is already booked on the selected date", exception.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_courtNotFound() {
        
        BookingRequest request = new BookingRequest(999L, LocalDate.of(2026, 7, 10), 2L);

        when(courtRepository.findById(999L)).thenReturn(Optional.empty());

        
        CustomException exception = assertThrows(CustomException.class, () -> bookingService.createBooking(request));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Court not found", exception.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approveBooking_success() {
        
        Long bookingId = 100L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .status("PENDING")
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        bookingService.approveBooking(bookingId);

        
        assertEquals("CONFIRMED", booking.getStatus());
        verify(bookingRepository).findById(bookingId);
        verify(bookingRepository).save(booking);
    }

    @Test
    void approveBooking_alreadyConfirmed() {
        
        Long bookingId = 100L;
        Booking booking = Booking.builder()
                .id(bookingId)
                .status("CONFIRMED")
                .build();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        
        CustomException exception = assertThrows(CustomException.class, () -> bookingService.approveBooking(bookingId));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Booking status must be PENDING to approve", exception.getMessage());

        verify(bookingRepository, never()).save(any(Booking.class));
    }
}

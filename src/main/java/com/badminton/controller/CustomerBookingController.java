package com.badminton.controller;

import com.badminton.dto.BookingDTO;
import com.badminton.dto.request.BookingRequest;
import com.badminton.dto.response.CustomerBookingHistoryDTO;
import com.badminton.dto.response.ResponseDTO;
import com.badminton.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/bookings")
public class CustomerBookingController {

    private final BookingService bookingService;

    public CustomerBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<BookingDTO>> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingDTO bookingDTO = bookingService.createBooking(request);
        ResponseDTO<BookingDTO> response = ResponseDTO.<BookingDTO>builder()
                .success(true)
                .message("Booking created successfully")
                .data(bookingDTO)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<List<CustomerBookingHistoryDTO>>> getBookingHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        List<CustomerBookingHistoryDTO> history = bookingService.getCustomerBookingHistory(email);
        ResponseDTO<List<CustomerBookingHistoryDTO>> response = ResponseDTO.<List<CustomerBookingHistoryDTO>>builder()
                .success(true)
                .message("Fetched customer booking history successfully")
                .data(history)
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ResponseDTO<Void>> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        ResponseDTO<Void> response = ResponseDTO.<Void>builder()
                .success(true)
                .message("Booking cancelled successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}

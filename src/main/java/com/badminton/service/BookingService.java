package com.badminton.service;

import com.badminton.dto.BookingDTO;
import com.badminton.dto.request.BookingRequest;
import com.badminton.dto.response.CustomerBookingHistoryDTO;

import java.util.List;

public interface BookingService {
    BookingDTO createBooking(BookingRequest request);
    List<CustomerBookingHistoryDTO> getCustomerBookingHistory(String email);
    void approveBooking(Long id);
    void rejectBooking(Long id);
    void cancelBooking(Long id);
}

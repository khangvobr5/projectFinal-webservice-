package com.badminton.controller;

import com.badminton.exception.CustomException;
import com.badminton.service.BookingService;
import com.badminton.service.RedisTokenBlacklistService;
import com.badminton.config.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.badminton.config.SecurityConfig;
import com.badminton.config.JwtRequestFilter;

@WebMvcTest(ManagerBookingController.class)
@Import({SecurityConfig.class, JwtRequestFilter.class})
class ManagerBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private RedisTokenBlacklistService redisTokenBlacklistService;

    @Test
    @WithMockUser(username = "manager@gmail.com", roles = "MANAGER")
    void approveBooking_success() throws Exception {
        doNothing().when(bookingService).approveBooking(1L);

        mockMvc.perform(put("/api/v1/manager/bookings/1/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Booking approved successfully"));
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", roles = "MANAGER")
    void approveBooking_notFound() throws Exception {
        doThrow(new CustomException("Booking not found", HttpStatus.NOT_FOUND))
                .when(bookingService).approveBooking(999L);

        mockMvc.perform(put("/api/v1/manager/bookings/999/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Booking not found"));
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", roles = "MANAGER")
    void rejectBooking_success() throws Exception {
        doNothing().when(bookingService).rejectBooking(1L);

        mockMvc.perform(put("/api/v1/manager/bookings/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Booking rejected successfully"));
    }

    @Test
    @WithMockUser(username = "manager@gmail.com", roles = "MANAGER")
    void rejectBooking_notFound() throws Exception {
        doThrow(new CustomException("Booking not found", HttpStatus.NOT_FOUND))
                .when(bookingService).rejectBooking(999L);

        mockMvc.perform(put("/api/v1/manager/bookings/999/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Booking not found"));
    }
}

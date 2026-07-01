package com.badminton.controller;

import com.badminton.dto.BookingDTO;
import com.badminton.dto.request.BookingRequest;
import com.badminton.dto.response.CustomerBookingHistoryDTO;
import com.badminton.exception.CustomException;
import com.badminton.service.BookingService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.badminton.config.SecurityConfig;
import com.badminton.config.JwtRequestFilter;

@WebMvcTest(CustomerBookingController.class)
@Import({SecurityConfig.class, JwtRequestFilter.class})
class CustomerBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private RedisTokenBlacklistService redisTokenBlacklistService;

    @Test
    @WithMockUser(username = "customer@gmail.com", roles = "CUSTOMER")
    void createBooking_success() throws Exception {
        BookingRequest request = new BookingRequest(1L, LocalDate.of(2026, 7, 10), 2L);
        BookingDTO bookingDTO = BookingDTO.builder()
                .id(100L)
                .courtId(1L)
                .timeSlotId(2L)
                .bookingDate(request.getBookingDate())
                .status("PENDING")
                .build();

        when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(bookingDTO);

        mockMvc.perform(post("/api/v1/customer/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100L))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "customer@gmail.com", roles = "CUSTOMER")
    void createBooking_conflict() throws Exception {
        BookingRequest request = new BookingRequest(1L, LocalDate.of(2026, 7, 10), 2L);

        when(bookingService.createBooking(any(BookingRequest.class)))
                .thenThrow(new CustomException("This time slot is already booked on the selected date", HttpStatus.CONFLICT));

        mockMvc.perform(post("/api/v1/customer/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("This time slot is already booked on the selected date"));
    }

    @Test
    @WithMockUser(username = "customer@gmail.com", roles = "CUSTOMER")
    void getBookingHistory_success() throws Exception {
        List<CustomerBookingHistoryDTO> historyList = List.of(
                CustomerBookingHistoryDTO.builder().bookingId(1L).courtName("Court A").status("CONFIRMED").build(),
                CustomerBookingHistoryDTO.builder().bookingId(2L).courtName("Court B").status("PENDING").build(),
                CustomerBookingHistoryDTO.builder().bookingId(3L).courtName("Court C").status("CANCELLED").build()
        );

        when(bookingService.getCustomerBookingHistory(eq("customer@gmail.com"))).thenReturn(historyList);

        mockMvc.perform(get("/api/v1/customer/bookings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].bookingId").value(1L))
                .andExpect(jsonPath("$.data[1].bookingId").value(2L))
                .andExpect(jsonPath("$.data[2].bookingId").value(3L));
    }
}

package com.badminton.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private Long id;
    private Long customerId;
    private Long courtId;
    private Long timeSlotId;
    private LocalDate bookingDate;
    private String status;
    private LocalDateTime createdAt;
}

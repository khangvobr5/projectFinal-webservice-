package com.badminton.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    @NotNull(message = "Court ID is required")
    private Long courtId;

    @NotNull(message = "Booking date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate bookingDate;

    @NotNull(message = "Time slot ID is required")
    private Long timeSlotId;
}

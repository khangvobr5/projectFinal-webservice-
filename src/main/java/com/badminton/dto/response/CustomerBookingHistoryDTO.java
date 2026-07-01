package com.badminton.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBookingHistoryDTO {
    private Long bookingId;
    private String courtName;
    private LocalDate bookingDate;
    private String timeSlot; 
    private String status;
}

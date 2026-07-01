package com.badminton.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourtDTO {
    private Long id;

    @NotBlank(message = "Court name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Price per hour is required")
    @Min(value = 0, message = "Price per hour must be greater than or equal to 0")
    private Double pricePerHour;

    @NotNull(message = "Manager ID is required")
    private Long managerId;

    private LocalDateTime createdAt;
}

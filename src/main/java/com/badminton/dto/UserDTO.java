package com.badminton.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    private String username;

    private String email;

    @Pattern(regexp = "^(CUSTOMER|MANAGER|ADMIN|OWNER)$", message = "Role must be one of: CUSTOMER, MANAGER, ADMIN, OWNER")
    private String role;

    private boolean active;

    private LocalDateTime createdAt;
}

package com.badminton.controller;

import com.badminton.dto.UserDTO;
import com.badminton.dto.request.*;
import com.badminton.dto.response.LoginResponse;
import com.badminton.dto.response.RefreshResponse;
import com.badminton.dto.response.ResponseDTO;
import com.badminton.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO<UserDTO>> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO registeredUser = authService.register(request);
        ResponseDTO<UserDTO> response = ResponseDTO.<UserDTO>builder()
                .success(true)
                .message("User registered successfully")
                .data(registeredUser)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse loginResponse = authService.login(request);
        ResponseDTO<LoginResponse> response = ResponseDTO.<LoginResponse>builder()
                .success(true)
                .message("Login successful")
                .data(loginResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDTO<RefreshResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        RefreshResponse refreshResponse = authService.refresh(request);
        ResponseDTO<RefreshResponse> response = ResponseDTO.<RefreshResponse>builder()
                .success(true)
                .message("Token refreshed successfully")
                .data(refreshResponse)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader);
        ResponseDTO<Void> response = ResponseDTO.<Void>builder()
                .success(true)
                .message("Logout successful")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ResponseDTO<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        authService.changePassword(email, request);
        ResponseDTO<Void> response = ResponseDTO.<Void>builder()
                .success(true)
                .message("Password changed successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseDTO<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String resetToken = authService.forgotPassword(request);
        ResponseDTO<String> response = ResponseDTO.<String>builder()
                .success(true)
                .message("Password reset token generated successfully")
                .data(resetToken)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseDTO<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        ResponseDTO<Void> response = ResponseDTO.<Void>builder()
                .success(true)
                .message("Password reset successfully")
                .build();
        return ResponseEntity.ok(response);
    }











}

package com.badminton.service;

import com.badminton.config.JwtUtil;
import com.badminton.dto.UserDTO;
import com.badminton.dto.request.ChangePasswordRequest;
import com.badminton.dto.request.ForgotPasswordRequest;
import com.badminton.dto.request.LoginRequest;
import com.badminton.dto.request.RefreshRequest;
import com.badminton.dto.request.RegisterRequest;
import com.badminton.dto.request.ResetPasswordRequest;
import com.badminton.dto.response.LoginResponse;
import com.badminton.dto.response.RefreshResponse;
import com.badminton.entity.User;
import com.badminton.exception.CustomException;
import com.badminton.repository.UserRepository;
import com.badminton.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTokenBlacklistService redisTokenBlacklistService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest("newuser", "newuser@test.com", "password123", "CUSTOMER");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = authService.register(request);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("newuser@test.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_emailExists() {
        RegisterRequest request = new RegisterRequest("newuser", "existing@test.com", "password123", "CUSTOMER");
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class, () -> authService.register(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Email already exists", exception.getMessage());
    }

    @Test
    void register_usernameExists() {
        RegisterRequest request = new RegisterRequest("existing", "newuser@test.com", "password123", "CUSTOMER");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class, () -> authService.register(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        assertEquals("Username already exists", exception.getMessage());
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("test@test.com", "password");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@test.com")
                .password("encodedPassword")
                .roles("CUSTOMER")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userDetailsService.loadUserByUsername("test@test.com")).thenReturn(userDetails);
        when(jwtUtil.generateAccessToken(any(UserDetails.class))).thenReturn("accessToken");
        when(jwtUtil.generateRefreshToken(any(UserDetails.class))).thenReturn("refreshToken");

        LoginResponse result = authService.login(request);

        assertNotNull(result);
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());
        assertEquals("Bearer", result.getTokenType());
    }

    @Test
    void refresh_success() {
        RefreshRequest request = new RefreshRequest("validRefreshToken");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@test.com")
                .password("encodedPassword")
                .roles("CUSTOMER")
                .build();

        when(redisTokenBlacklistService.isBlacklisted(anyString())).thenReturn(false);
        when(jwtUtil.extractUsername(anyString())).thenReturn("test@test.com");
        when(userDetailsService.loadUserByUsername("test@test.com")).thenReturn(userDetails);
        when(jwtUtil.validateToken(anyString(), any(UserDetails.class))).thenReturn(true);
        when(jwtUtil.generateAccessToken(any(UserDetails.class))).thenReturn("newAccessToken");

        RefreshResponse result = authService.refresh(request);

        assertNotNull(result);
        assertEquals("newAccessToken", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
    }

    @Test
    void refresh_tokenBlacklisted() {
        RefreshRequest request = new RefreshRequest("blacklistedToken");
        when(redisTokenBlacklistService.isBlacklisted("blacklistedToken")).thenReturn(true);

        CustomException exception = assertThrows(CustomException.class, () -> authService.refresh(request));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Refresh token is blacklisted", exception.getMessage());
    }

    @Test
    void changePassword_success() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword123");
        User user = User.builder().id(1L).email("test@test.com").password("encodedOldPassword").build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.changePassword("test@test.com", request);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_wrongOldPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrongPassword", "newPassword123");
        User user = User.builder().id(1L).email("test@test.com").password("encodedOldPassword").build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        CustomException exception = assertThrows(CustomException.class, () -> authService.changePassword("test@test.com", request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Old password is incorrect", exception.getMessage());
    }

    @Test
    void forgotPassword_success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@test.com");
        User user = User.builder().id(1L).email("test@test.com").build();

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String token = authService.forgotPassword(request);

        assertNotNull(token);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void resetPassword_success() {
        ResetPasswordRequest request = new ResetPasswordRequest("validToken", "newPassword123");
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .resetToken("validToken")
                .resetTokenExpiredAt(LocalDateTime.now().plusMinutes(10))
                .build();

        when(userRepository.findByResetToken("validToken")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.resetPassword(request);

        assertNull(user.getResetToken());
        assertNull(user.getResetTokenExpiredAt());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void resetPassword_expiredToken() {
        ResetPasswordRequest request = new ResetPasswordRequest("expiredToken", "newPassword123");
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .resetToken("expiredToken")
                .resetTokenExpiredAt(LocalDateTime.now().minusMinutes(10))
                .build();

        when(userRepository.findByResetToken("expiredToken")).thenReturn(Optional.of(user));

        CustomException exception = assertThrows(CustomException.class, () -> authService.resetPassword(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("Reset token has expired", exception.getMessage());
    }
}

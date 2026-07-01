package com.badminton.service.impl;

import com.badminton.config.JwtUtil;
import com.badminton.dto.UserDTO;
import com.badminton.dto.request.*;
import com.badminton.dto.response.LoginResponse;
import com.badminton.dto.response.RefreshResponse;
import com.badminton.entity.User;
import com.badminton.exception.CustomException;
import com.badminton.repository.UserRepository;
import com.badminton.service.AuthService;
import com.badminton.service.RedisTokenBlacklistService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTokenBlacklistService redisTokenBlacklistService;
    private final UserDetailsService userDetailsService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtil jwtUtil,
                           RedisTokenBlacklistService redisTokenBlacklistService,
                           UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.redisTokenBlacklistService = redisTokenBlacklistService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("Email is already registered", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException("Username is already taken", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole().toUpperCase())
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        return UserDTO.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .active(savedUser.isActive())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshResponse refresh(RefreshRequest request) {
        String token = request.getRefreshToken();
        try {
            if (jwtUtil.isTokenExpired(token)) {
                throw new CustomException("Refresh token is expired", HttpStatus.UNAUTHORIZED);
            }
            if (redisTokenBlacklistService.isBlacklisted(token)) {
                throw new CustomException("Refresh token is blacklisted", HttpStatus.UNAUTHORIZED);
            }

            String email = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtUtil.validateToken(token, userDetails)) {
                throw new CustomException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
            }

            String newAccessToken = jwtUtil.generateAccessToken(userDetails);
            return RefreshResponse.builder()
                    .accessToken(newAccessToken)
                    .tokenType("Bearer")
                    .build();
        } catch (Exception e) {
            throw new CustomException("Invalid refresh token", HttpStatus.UNAUTHORIZED);
        }
    }

    @Override
    @Transactional
    public void logout(String authorizationHeader) {
        if (authorizationHeader == null) {
            throw new CustomException("Missing Authorization header", HttpStatus.BAD_REQUEST);
        }

        String token = authorizationHeader.trim();
        while (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }

        if (token.isEmpty()) {
            throw new CustomException("Token is empty", HttpStatus.BAD_REQUEST);
        }

        try {
            Date expiration = jwtUtil.extractExpiration(token);
            long remainingTime = expiration.getTime() - System.currentTimeMillis();

            
            
            if (remainingTime > 0) {
                redisTokenBlacklistService.addToBlacklist(token, remainingTime);
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            
        } catch (Exception e) {
            throw new CustomException("Invalid access token", HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found", HttpStatus.NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new CustomException("Incorrect old password", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("User not found with email: " + request.getEmail(), HttpStatus.NOT_FOUND));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiredAt(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        return token;
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getResetToken())
                .orElseThrow(() -> new CustomException("Invalid or expired reset token", HttpStatus.NOT_FOUND));

        if (user.getResetTokenExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CustomException("Reset token has expired", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiredAt(null);
        userRepository.save(user);
    }











}

package com.badminton.controller;

import com.badminton.dto.request.LoginRequest;
import com.badminton.dto.response.LoginResponse;
import com.badminton.service.AuthService;
import com.badminton.service.RedisTokenBlacklistService;
import com.badminton.config.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.context.annotation.Import;
import com.badminton.config.SecurityConfig;
import com.badminton.config.JwtRequestFilter;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtRequestFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private RedisTokenBlacklistService redisTokenBlacklistService;

    @Test
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest("customer@gmail.com", "password123");
        LoginResponse response = LoginResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void login_badCredentials() throws Exception {
        LoginRequest request = new LoginRequest("customer@gmail.com", "wrong-password");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}

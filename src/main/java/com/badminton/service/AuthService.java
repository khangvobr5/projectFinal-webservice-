package com.badminton.service;

import com.badminton.dto.UserDTO;
import com.badminton.dto.request.*;
import com.badminton.dto.response.LoginResponse;
import com.badminton.dto.response.RefreshResponse;

public interface AuthService {
    UserDTO register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    RefreshResponse refresh(RefreshRequest request);
    void logout(String authorizationHeader);
    void changePassword(String email, ChangePasswordRequest request);
    String forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);

}

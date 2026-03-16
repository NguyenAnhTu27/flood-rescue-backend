package com.floodrescue.module.auth.service;

import com.floodrescue.module.auth.dto.request.LoginRequest;
import com.floodrescue.module.auth.dto.request.ForgotPasswordRequest;
import com.floodrescue.module.auth.dto.request.RegisterCitizenRequest;
import com.floodrescue.module.auth.dto.request.ResetPasswordRequest;
import com.floodrescue.module.auth.dto.response.ForgotPasswordResponse;
import com.floodrescue.module.auth.dto.response.LoginResponse;
import com.floodrescue.module.auth.dto.response.UserProfileResponse;

public interface AuthService {
    void registerCitizen(RegisterCitizenRequest req);
    LoginResponse login(LoginRequest req);
    LoginResponse registerAndLogin(RegisterCitizenRequest req);
    LoginResponse refreshByRefreshToken(String refreshToken);
    void logoutByRefreshToken(String refreshToken);
    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest req);
    void resetPassword(ResetPasswordRequest req);
    UserProfileResponse getUserProfile(Long userId);
}
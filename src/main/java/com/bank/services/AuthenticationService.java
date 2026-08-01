package com.bank.services;

import com.bank.dtos.requestDtos.*;
import com.bank.dtos.responseDtos.LoginResponse;
import com.bank.dtos.responseDtos.SelectInstitutionResponse;
import com.bank.dtos.requestDtos.ForgotPasswordRequest;
import com.bank.dtos.requestDtos.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletRequest;


public interface AuthenticationService {
    void registerInstitution(final RegisterInstitutionRequest registerInstitutionRequest);
    void verifyInstOtp(OtpVerifyRequest request);
    void resendOtp(OtpRequest request);
    void createUser(RegisterUserRequest registerUserRequest);
    void verifyUserOtp(OtpVerifyRequest request);
    SelectInstitutionResponse preLogin(final SelectInstitutionRequest request);
    LoginResponse login(LoginRequest request, HttpServletRequest httpServletRequest);
    LoginResponse refreshToken(final RefreshTokenRequest refreshTokenRequest);
    void forgotPassword(ForgotPasswordRequest request);
    String verifyResetOtp(VerifyResetOtpRequest request);
    void resetPassword(String token, ResetPasswordRequest request);
}

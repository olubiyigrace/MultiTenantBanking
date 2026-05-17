package com.bank.auth.service;

import com.bank.auth.requests.ChangePasswordRequest;
import com.bank.auth.requests.LoginRequest;
import com.bank.auth.requests.RefreshTokenRequest;
import com.bank.auth.response.LoginResponse;
import com.bank.requests.RegisterInstitutionRequest;
import com.bank.requests.RegisterUserRequest;
import jakarta.mail.MessagingException;

public interface AuthenticationService {
    void registerInstitution(final RegisterInstitutionRequest registerInstitutionRequest) throws MessagingException;
    void verifyEmail(final String verificationTokenFromRequest, final String email);
    void resendEmailVerificationToken(final String email);
    LoginResponse login(final LoginRequest request) throws MessagingException;
    void createUser(RegisterUserRequest registerUserRequest) throws MessagingException;
    LoginResponse refreshToken(final RefreshTokenRequest refreshTokenRequest);
    void changePassword(ChangePasswordRequest request);
}

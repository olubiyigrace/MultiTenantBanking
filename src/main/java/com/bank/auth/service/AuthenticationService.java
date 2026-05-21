package com.bank.auth.service;

import com.bank.auth.requests.*;
import com.bank.auth.response.LoginResponse;
import com.bank.requests.RegisterInstitutionRequest;
import com.bank.auth.requests.RegisterUserRequest;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    void registerInstitution(final RegisterInstitutionRequest registerInstitutionRequest) throws MessagingException;
    void verifyEmail(final String verificationTokenFromRequest, final String email);
    void resendEmailVerificationToken(final String email);
    LoginResponse login(final LoginRequest request) throws MessagingException;
    void createUser(RegisterUserRequest registerUserRequest) throws MessagingException;
    void verifyUser(final String verificationTokenFromRequest, final String email);
    void resendUserVerificationToken(final String email);
    LoginResponse refreshToken(final RefreshTokenRequest refreshTokenRequest);
    void changePassword(ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request) throws MessagingException;
    void resetPasswordWithToken(String token, ResetPasswordRequest request);
    void logout(HttpServletRequest request);
}

package com.bank.others.auth;

import com.bank.institutions.RegisterInstitutionRequest;
import com.bank.users.RegisterUserRequest;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    void registerInstitution(final RegisterInstitutionRequest registerInstitutionRequest) throws MessagingException;
    void verifyEmail(final String verificationTokenFromRequest, final String email);
    void reverifyInstitutionEmail(final String email);
    void createUser(RegisterUserRequest registerUserRequest) throws MessagingException;
    LoginResponse login(final LoginRequest request) throws MessagingException;
    void verifyUser(final String verificationTokenFromRequest, final String email);
    void resendUserVerificationToken(final String email);
    LoginResponse refreshToken(final RefreshTokenRequest refreshTokenRequest);
    void changePassword(ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request) throws MessagingException;
    void resetPasswordWithToken(String token, ResetPasswordRequest request);
    void logout(HttpServletRequest request);
    void revokeSession(String token);
}

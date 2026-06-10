package com.bank.others.auth;

import com.bank.institutions.RegisterInstitutionRequest;
import com.bank.others.login.*;
import com.bank.others.password.ChangePasswordRequest;
import com.bank.others.password.ForgotPasswordRequest;
import com.bank.others.password.ResetPasswordRequest;
import com.bank.users.RegisterUserRequest;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationService {
    void registerInstitution(final RegisterInstitutionRequest registerInstitutionRequest) throws MessagingException;
    void verifyEmail(final String verificationTokenFromRequest, final String email);
    void reverifyInstitutionEmail(final String email);
    void createUser(RegisterUserRequest registerUserRequest) throws MessagingException;
    SelectInstitutionResponse preLogin(final SelectInstitutionRequest request);
    LoginResponse login(LoginRequest request) throws MessagingException;
    void verifyUser(final String verificationTokenFromRequest, final String email);
    void resendUserVerificationToken(final String email);
    LoginResponse refreshToken(final RefreshTokenRequest refreshTokenRequest);
    void changePassword(ChangePasswordRequest request);
    void forgotPassword(ForgotPasswordRequest request) throws MessagingException;
    void resetPasswordWithToken(String token, ResetPasswordRequest request);
    void logout(HttpServletRequest request);
    void revokeSession(String token);
}

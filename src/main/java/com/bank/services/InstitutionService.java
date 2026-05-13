package com.bank.services;

import com.bank.requests.LoginRequest;
import com.bank.requests.RegisterInstitutionRequest;
import com.bank.responses.InstitutionResponse;
import com.bank.utils.TokenPair;
import jakarta.mail.MessagingException;

public interface InstitutionService {
    void registerInstitution(final RegisterInstitutionRequest registerInstitutionRequest) throws MessagingException;
    void verifyEmail(final String verificationTokenFromRequest, String email);
    void resendEmailVerificationToken(final String email);
    TokenPair adminLogin(LoginRequest loginRequest) throws MessagingException;
}

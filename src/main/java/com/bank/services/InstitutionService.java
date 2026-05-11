package com.bank.services;

import com.bank.dto.RegisterInstitutionRequest;
import jakarta.mail.MessagingException;

public interface InstitutionService {
    void registerInstitution(final RegisterInstitutionRequest registerInstitutionRequest) throws MessagingException;
    void verifyEmail(final String verificationTokenFromRequest, String email);
    void resendEmailVerificationToken(final String email);

}

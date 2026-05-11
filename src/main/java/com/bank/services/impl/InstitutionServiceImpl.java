package com.bank.services.impl;

import com.bank.dto.RegisterInstitutionRequest;
import com.bank.entities.Institution;
import com.bank.enums.InstitutionStatus;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.exceptions.InvalidRequestException;
import com.bank.exceptions.UnauthorizedException;
import com.bank.repositories.InstitutionRepository;
import com.bank.services.EmailService;
import com.bank.services.InstitutionService;
import com.bank.mapper.InstitutionMapper;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {
    private final InstitutionRepository institutionRepository;
    private final InstitutionMapper institutionMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void registerInstitution(RegisterInstitutionRequest registerInstitutionRequest) throws MessagingException {
        if (institutionRepository.existsByRcNumber(registerInstitutionRequest.getRcNumber())){
            throw new DuplicateResourceException("Institution already exists.");
        }
         if (institutionRepository.existsByEmail(registerInstitutionRequest.getEmail())){
            throw new DuplicateResourceException("Institution email already exists.");
        }
        final Institution institution = institutionMapper.toEntity(registerInstitutionRequest);
         institution.setStatus(InstitutionStatus.PENDING);
        String emailVerificationToken = UUID.randomUUID().toString();
        institution.setEmailVerificationToken(passwordEncoder.encode(emailVerificationToken));
        institution.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));
        institutionRepository.save(institution);

        Map<String, Object> model = new HashMap<>();
        model.put("name", registerInstitutionRequest.getName());
        model.put("verificationUrl", "https://multitenantbanking.com/api/v1/auth/verify?token=" + emailVerificationToken);

        emailService.sendVerificationEmail(
                registerInstitutionRequest.getEmail(),
                "Verify your account",
                "verification", // template name without `.html`
    model
        );
}

@Override
@Transactional
    public void verifyEmail(final String verificationTokenFromRequest, final String email) {
    Institution institution = institutionRepository.findByEmail(email)
            .orElseThrow(() -> new InvalidRequestException("Institution with the email '" + email + "' does not exist. Visit the website to register"));
    if (!passwordEncoder.matches(verificationTokenFromRequest, institution.getEmailVerificationToken())) {
        throw new InvalidRequestException("Invalid token!");
    }
    if (institution.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
        throw new RuntimeException("Token has expired!");
    }
    institution.setEmailVerifiedAt(LocalDateTime.now());
    institution.setIsVerified(true);
    institution.setEmailVerifiedAt(LocalDateTime.now());
    institution.setEmailVerificationToken(null);
    institution.setEmailVerificationTokenExpiry(null);
    institutionRepository.save(institution);
}

@Override
@Transactional
    public void resendEmailVerificationToken(final String email) {
        Institution institution = institutionRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("user with the email '" + email + "'  does not exist. Visit the website to create an account."));

        if (Boolean.TRUE.equals(institution.getIsVerified())) {
            throw new DuplicateResourceException("User already verified!");
        }
        String emailVerificationToken = UUID.randomUUID().toString();
        institution.setEmailVerificationToken(passwordEncoder.encode(emailVerificationToken));
        institution.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));
        institutionRepository.save(institution);

        Map<String, Object> model = new HashMap<>();
        model.put("name", institution.getName());
        model.put("verificationUrl", "https://multitenantbanking.com/api/v1/auth/resend-verification?token=" + emailVerificationToken);
        try {
            emailService.sendVerificationEmail(
                    institution.getEmail(),
                    "Verify your account",
                    "verification",
                    model
            );
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}

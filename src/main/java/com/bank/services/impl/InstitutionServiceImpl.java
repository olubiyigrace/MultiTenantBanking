package com.bank.services.impl;

import com.bank.requests.LoginRequest;
import com.bank.requests.RegisterInstitutionRequest;
import com.bank.entities.Institution;
import com.bank.enums.InstitutionStatus;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.exceptions.InvalidRequestException;
import com.bank.exceptions.UnauthorizedException;
import com.bank.repositories.InstitutionRepository;
import com.bank.services.EmailService;
import com.bank.services.InstitutionService;
import com.bank.mapper.InstitutionMapper;
import com.bank.services.JwtService;
import com.bank.utils.TokenPair;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {
    private final InstitutionRepository institutionRepository;
    private final InstitutionMapper institutionMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public void registerInstitution(final RegisterInstitutionRequest registerInstitutionRequest) throws MessagingException {
        if (institutionRepository.existsByRcNumber(registerInstitutionRequest.getRcNumber())){
            log.debug("Institution with the RC Number '{}' has been registered.", registerInstitutionRequest.getRcNumber());
            throw new DuplicateResourceException("Institution with the RC Number '" + registerInstitutionRequest.getRcNumber() + "' has been registered.");
        }
         if (institutionRepository.existsByEmail(registerInstitutionRequest.getEmail())){
             log.debug("Institution with the email '{}' has been registered.", registerInstitutionRequest.getEmail());
             throw new DuplicateResourceException("Institution with the email '" + registerInstitutionRequest.getEmail() + "' has been registered.");
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
        log.debug("Invalid token!");
        throw new InvalidRequestException("Invalid token!");
    }
    if (institution.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
        log.debug("Token has expired!");
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
            log.debug("User already verified!");
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
    @Transactional
    public TokenPair adminLogin(final LoginRequest loginRequest) throws MessagingException {
        Optional<Institution> institution = institutionRepository.findByAdminEmail(loginRequest.getAdminEmail());
        if (institution.isEmpty()) throw new EntityNotFoundException("Institution admin email '" + loginRequest.getAdminEmail() + "' not found");
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getAdminEmail(),
                        loginRequest.getAdminPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Map<String, Object> model = new HashMap<>();
        model.put("name", loginRequest.getAdminEmail());
        model.put("resetPassword", "https://multitenantbank.com/api/v1/reset-password?token=");

        emailService.sendVerificationEmail(
                loginRequest.getAdminEmail(),
                "New Login Alert!",
                "login",
                model
        );
        return jwtService.generateTokenPair(authentication);
    }
}

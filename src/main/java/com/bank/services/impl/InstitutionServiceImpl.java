package com.bank.services.impl;

import com.bank.common.PageResponse;
import com.bank.entities.User;
import com.bank.enums.UserAccountType;
import com.bank.repositories.UserRepository;
import com.bank.requests.RegisterInstitutionRequest;
import com.bank.entities.Institution;
import com.bank.enums.InstitutionStatus;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.exceptions.InvalidRequestException;
import com.bank.exceptions.UnauthorizedException;
import com.bank.repositories.InstitutionRepository;
import com.bank.responses.InstitutionResponse;
import com.bank.services.EmailService;
import com.bank.services.InstitutionService;
import com.bank.mapper.InstitutionMapper;
import com.bank.services.ProvisioningService;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    private final UserRepository userRepository;
    private final InstitutionMapper institutionMapper;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final ProvisioningService provisioningService;

    @Override
    @Transactional
    public void registerInstitution(final RegisterInstitutionRequest registerInstitutionRequest) throws MessagingException {
        if (institutionRepository.existsByRcNumber(registerInstitutionRequest.getCompanyRcNumber())) {
            log.debug("Institution with the RC Number '{}' has been registered.", registerInstitutionRequest.getCompanyRcNumber());
            throw new DuplicateResourceException("Institution with the RC Number '" + registerInstitutionRequest.getCompanyRcNumber() + "' already exists..");
        }
        if (institutionRepository.existsByEmail(registerInstitutionRequest.getCompanyEmail())) {
            log.debug("Institution with the email '{}' has been registered.", registerInstitutionRequest.getCompanyEmail());
            throw new DuplicateResourceException("Institution with the email '" + registerInstitutionRequest.getCompanyEmail() + "' already exists.");
        }
        final Institution institution = institutionMapper.toEntity(registerInstitutionRequest);
        institution.setInstitutionStatus(InstitutionStatus.PENDING);
        String emailVerificationToken = UUID.randomUUID().toString();
        institution.setEmailVerificationToken(passwordEncoder.encode(emailVerificationToken));
        institution.setEmailVerificationTokenExpiry(LocalDateTime.now().plusMinutes(10));
        institutionRepository.save(institution);

        Map<String, Object> model = new HashMap<>();
        model.put("name", registerInstitutionRequest.getCompanyName());
        model.put("verificationUrl", "https://multitenantbanking.com/api/v1/auth/verify?token=" + emailVerificationToken);

        emailService.sendVerificationEmail(
                registerInstitutionRequest.getCompanyEmail(),
                "Verify your account",
                "verification",
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
        model.put("companyName", institution.getCompanyName());
        model.put("verificationUrl", "https://multitenantbanking.com/api/v1/auth/resend-verification?token=" + emailVerificationToken);
        try {
            emailService.sendVerificationEmail(
                    institution.getCompanyEmail(),
                    "Verify your account",
                    "verification",
                    model
            );
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void approveInstitution(final String institutionId){
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        if (institution.getInstitutionStatus() != InstitutionStatus.PENDING) {
            throw new InvalidRequestException("Institution is not in pending status");
        }
        institution.setInstitutionStatus(InstitutionStatus.ACTIVE);
        institutionRepository.save(institution);
        try{
           provisioningService.provisionInstitution(institution);
           createAdminUser(institution);
        } catch (final Exception e){
            rollBackInstitutionStatus(institution);
        }
    }

    @Override
    public void activateInstitution(String institutionId) {
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        if (institution.getInstitutionStatus() != InstitutionStatus.PENDING) {
            throw new InvalidRequestException("Institution is not in pending status");
        }
        institution.setInstitutionStatus(InstitutionStatus.ACTIVE);
        institutionRepository.save(institution);
    }

    @Override
    public void deactivateInstitution(String institutionId) {
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        if (institution.getInstitutionStatus() != InstitutionStatus.ACTIVE) {
            throw new InvalidRequestException("Institution is not in pending status");
        }
        institution.setInstitutionStatus(InstitutionStatus.INACTIVE);
        institutionRepository.save(institution);
    }

    @Override
    public void suspendInstitution(String institutionId) {
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        if (institution.getInstitutionStatus() != InstitutionStatus.ACTIVE) {
            throw new InvalidRequestException("Institution is not in pending status");
        }
        institution.setInstitutionStatus(InstitutionStatus.SUSPENDED);
        institutionRepository.save(institution);
    }

    @Override
    public PageResponse<InstitutionResponse> findAll(int page, int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<Institution> institutions = institutionRepository.findAll(pageRequest);
        final Page<InstitutionResponse> institutionResponse = institutions.map(institutionMapper::toResponse);
        return PageResponse.of(institutionResponse);
    }

    private void createAdminUser(Institution institution) {
        if (userRepository.existsByUsername(institution.getAdminUsername())){
            log.debug("User already exists!");
            throw new DuplicateResourceException("User already exists!");
        }
        final User adminUser = User.builder()
                .username(institution.getAdminUsername())
                .email(institution.getAdminEmail())
                .name(institution.getAdminName())
                .password(institution.getAdminPassword())
                .userAccountType(UserAccountType.INSTITUTION_ADMIN)
                .institution(institution)
                .build();
        userRepository.save(adminUser);
        log.info("Admin user created successfully");
    }

    private void rollBackInstitutionStatus(Institution institution) {
        institution.setInstitutionStatus(InstitutionStatus.PENDING);
        institutionRepository.save(institution);
    }
}

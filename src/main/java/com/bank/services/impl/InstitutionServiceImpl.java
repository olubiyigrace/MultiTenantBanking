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
    private final ProvisioningService provisioningService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public void approveInstitution(final String institutionId) {
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        institutionRepository.save(institution);
        try {
            provisioningService.provisionInstitution(institution);
            createAdminUser(institution);
        } catch (final Exception e) {
                log.error("Institution approval failed", e);
                rollBackInstitutionStatus(institution);
                throw e;
            }
    }

    @Override
    public void activateInstitution(String institutionId) {
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        if (institution.getInstitutionStatus() != InstitutionStatus.PENDING) {
            log.debug("Institution is not pending");
            throw new InvalidRequestException("Institution is not pending");
        }
        institution.setInstitutionStatus(InstitutionStatus.ACTIVE);
        institutionRepository.save(institution);
    }

    @Override
    public void deactivateInstitution(String institutionId) {
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        if (institution.getInstitutionStatus() != InstitutionStatus.ACTIVE) {
            log.debug("Institution is pending");
            throw new InvalidRequestException("Institution is pending");
        }
        institution.setInstitutionStatus(InstitutionStatus.INACTIVE);
        institutionRepository.save(institution);
    }

    @Override
    public void suspendInstitution(String institutionId) {
        final Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new EntityNotFoundException("Institution with the id '" + institutionId + "' does not exist"));
        if (institution.getInstitutionStatus() != InstitutionStatus.ACTIVE) {
            throw new InvalidRequestException("Institution is pending");
        }
        institution.setInstitutionStatus(InstitutionStatus.SUSPENDED);
        institutionRepository.save(institution);
    }

    @Override
    public PageResponse<InstitutionResponse> findAllInstitution(int page, int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<Institution> institutions = institutionRepository.findAll(pageRequest);
        final Page<InstitutionResponse> institutionResponse = institutions.map(institutionMapper::toResponse);
        return PageResponse.of(institutionResponse);
    }

    private void rollBackInstitutionStatus(Institution institution) {
        institution.setInstitutionStatus(InstitutionStatus.PENDING);
        institutionRepository.save(institution);
        log.debug("Institution not approved");
    }
    private void createAdminUser(Institution institution) {
        if (userRepository.existsByUsername(institution.getAdminUsername())){
            log.debug("User already exists");
            throw new DuplicateResourceException("User already exists");
        }
        final User adminUser = User.builder()
                .username(institution.getAdminUsername())
                .email(institution.getAdminEmail())
                .name(institution.getAdminName())
                .nin(institution.getAdminNin())
                .phone(institution.getAdminPhone())
                .password(institution.getAdminPassword())
                .userAccountType(UserAccountType.INSTITUTION_ADMIN)
                .institution(institution)
                .build();
        userRepository.save(adminUser);
        log.info("Admin user created successfully");
    }
}

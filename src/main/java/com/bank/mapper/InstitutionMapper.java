package com.bank.mapper;

import com.bank.dto.InstitutionResponse;
import com.bank.dto.RegisterInstitutionRequest;
import com.bank.entities.Institution;
import com.bank.enums.InstitutionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class InstitutionMapper {
    private final PasswordEncoder passwordEncoder;

    public Institution toEntity(final RegisterInstitutionRequest institutionRequest){
        return Institution.builder()
                .name(institutionRequest.getName())
                .email(institutionRequest.getEmail())
                .phone(institutionRequest.getPhone())
                .rcNumber(institutionRequest.getRcNumber())
                .institutionType(institutionRequest.getInstitutionType())
                .adminName(institutionRequest.getAdminName())
                .adminEmail(institutionRequest.getAdminEmail())
                .adminNin(institutionRequest.getAdminNin())
                .adminPhone(institutionRequest.getAdminPhone())
                .isVerified(false)
                .adminPassword(passwordEncoder.encode(institutionRequest.getAdminPassword()))
                .build();
    }
    public InstitutionResponse toResponse(final Institution institution){
        return InstitutionResponse.builder()
                .id(institution.getId())
                .name(institution.getName())
                .email(institution.getEmail())
                .phone(institution.getPhone())
                .rcNumber(institution.getRcNumber())
                .institutionType(institution.getInstitutionType())
                .status(institution.getStatus())
                .adminName(institution.getAdminName())
                .adminEmail(institution.getAdminEmail())
                .adminPhone(institution.getAdminPhone())
                .adminNin(institution.getAdminNin())
                .build();
    }
}

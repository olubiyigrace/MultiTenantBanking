package com.bank.institutions;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InstitutionMapper {
    private final PasswordEncoder passwordEncoder;

    public Institution toEntity(final RegisterInstitutionRequest institutionRequest){
        return Institution.builder()
                .institutionName(institutionRequest.getInstitutionName())
                .institutionEmail(institutionRequest.getInstitutionEmail())
                .institutionPhone(institutionRequest.getInstitutionPhone())
                .institutionRcNumber(institutionRequest.getInstitutionRcNumber())
                .institutionCode(institutionRequest.getInstitutionCode())
                .institutionType(institutionRequest.getInstitutionType())
                .adminName(institutionRequest.getAdminName())
                .adminEmail(institutionRequest.getAdminEmail())
                .adminNin(institutionRequest.getAdminNin())
                .adminPhone(institutionRequest.getAdminPhone())
                .adminUsername(institutionRequest.getAdminEmail())
                .adminPassword(passwordEncoder.encode(institutionRequest.getAdminPassword()))
                .build();
    }
    public InstitutionResponse toResponse(final Institution institution){
        return InstitutionResponse.builder()
                .id(institution.getId())
                .name(institution.getInstitutionName())
                .email(institution.getInstitutionEmail())
                .phone(institution.getInstitutionPhone())
                .rcNumber(institution.getInstitutionRcNumber())
                .institutionType(institution.getInstitutionType())
                .status(institution.getInstitutionStatus())
                .adminName(institution.getAdminName())
                .adminEmail(institution.getAdminEmail())
                .adminPhone(institution.getAdminPhone())
                .adminNin(institution.getAdminNin())
                .build();
    }
}

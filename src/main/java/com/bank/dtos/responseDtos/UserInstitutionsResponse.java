package com.bank.dtos.responseDtos;

import com.bank.enums.InstitutionStatus;
import com.bank.enums.InstitutionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInstitutionsResponse {
    private String institutionId;
    private String institutionName;
    private InstitutionType institutionType;
    private InstitutionStatus institutionStatus;
}

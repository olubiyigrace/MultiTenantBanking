package com.bank.others.auth;

import com.bank.institutions.InstitutionStatus;
import com.bank.institutions.InstitutionType;
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

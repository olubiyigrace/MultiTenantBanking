package com.bank.institutions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class InstitutionResponse {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String rcNumber;
    private InstitutionType institutionType;
    private InstitutionStatus status;
    private String adminName;
    private String adminEmail;
    private String adminPhone;
    private String adminNin;
}

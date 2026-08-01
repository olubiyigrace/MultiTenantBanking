package com.bank.dtos.responseDtos;

import com.bank.enums.BaseCurrency;
import com.bank.enums.InstitutionStatus;
import com.bank.enums.InstitutionType;
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
    private BaseCurrency baseCurrency;
    private InstitutionStatus status;
    private String adminName;
    private String adminEmail;
    private String adminPhone;
    private String adminNin;
}

package com.bank.dtos.responseDtos;

import com.bank.enums.ProfileStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MemberResponse {
    private String id;
    private String bvn;
    private String address;
    private String employmentStatus;
    private BigDecimal monthlyIncome;
    private String nextOfKinName;
    private String nextOfKinPhone;
    private LocalDate dateOfBirth;
    private ProfileStatus profileStatus;
    private UserResponse userResponse;
}

package com.bank.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class MemberRequest {
    private String bvn;
    private String address;
    private String employmentStatus;
    private BigDecimal monthlyIncome;
    private String nextOfKinName;
    private String nextOfKinPhone;
    private LocalDate dateOfBirth;
}

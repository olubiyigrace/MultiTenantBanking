package com.bank.dtos.requestDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyResetOtpRequest {
    private String email;
    private String otp;
}

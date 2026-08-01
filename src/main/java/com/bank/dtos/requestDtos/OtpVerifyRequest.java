package com.bank.dtos.requestDtos;

import com.bank.enums.OtpPurpose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpVerifyRequest {
    private String otp;
    private String email;
    private OtpPurpose purpose;
}

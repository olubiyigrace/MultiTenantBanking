package com.bank.dtos.requestDtos;

import com.bank.enums.OtpPurpose;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtpRequest {
    private String email;
    private OtpPurpose purpose;
}

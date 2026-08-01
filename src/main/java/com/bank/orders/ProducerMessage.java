package com.bank.orders;

import com.bank.enums.OtpPurpose;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProducerMessage {
    private String email;
    private String otp;
    private OtpPurpose purpose;
}

package com.bank.loanguarantors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class GuarantorResponse {
    private String id;
    private String guarantorMemberId;
    private GuarantorStatus guarantorStatus;
    private LocalDateTime respondedAt;
}

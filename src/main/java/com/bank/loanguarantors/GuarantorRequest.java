package com.bank.loanguarantors;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GuarantorRequest {
    @NotNull(message = "Guarantor member Id must be provided")
    private String guarantorMemberId;
}

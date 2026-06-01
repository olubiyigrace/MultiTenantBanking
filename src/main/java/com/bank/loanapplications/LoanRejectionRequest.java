package com.bank.loanapplications;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data@AllArgsConstructor
public class LoanRejectionRequest {
    @NotNull(message = "Rejection reason must be stated")
    private String loanRejectionReason;
}

package com.bank.dtos.requestDtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReversalRequest {
    @NotBlank(message = "Transaction id is required")
    private String reversedByTransactionId;

}

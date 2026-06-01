package com.bank.loancollaterals;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class LoanCollateralRequest {
    @NotBlank(message = "Description should not be empty")
    private String description;

    @URL
    @NotBlank(message = "Document url should not be empty")
    private String documentUrl;

    private BigDecimal estimatedValue;
}

package com.bank.loancollaterals;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoanCollateralResponse {
    private String id;
    private String description;
    private BigDecimal estimatedValue;
    private String documentUrl;
}

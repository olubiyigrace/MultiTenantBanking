package com.bank.others.utils.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class TotalSavingsStatisticsResponse {
    private BigDecimal totalInstitutionsSavingsBalance;
    private List<TotalSavingsResponse> institutions;
}

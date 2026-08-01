package com.bank.services;

import com.bank.dtos.requestDtos.LoanCollateralRequest;

public interface CollateralService {
    void createCollateral(LoanCollateralRequest loanCollateralRequest);
    void deleteCollateral(String loanCollateralId);
}

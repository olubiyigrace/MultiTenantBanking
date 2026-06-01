package com.bank.loancollaterals;

public interface CollateralService {
    void createCollateral(LoanCollateralRequest loanCollateralRequest);
    void deleteCollateral(String loanCollateralId);
}

package com.bank.loancollaterals;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CollateralRepository extends JpaRepository<LoanCollateral, String> {
    LoanCollateral findByLoanApplicationId(String loanApplicationId);
}

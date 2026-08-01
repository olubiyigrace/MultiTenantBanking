package com.bank.repositories;

import com.bank.entities.LoanCollateral;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollateralRepository extends JpaRepository<LoanCollateral, String> {
    LoanCollateral findByLoanApplicationId(String loanApplicationId);
}

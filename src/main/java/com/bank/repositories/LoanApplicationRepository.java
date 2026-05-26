package com.bank.repositories;

import com.bank.entities.LoanApplication;
import com.bank.enums.LoanApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository <LoanApplication, String> {
    boolean existsByMemberIdAndLoanStatus(String memberId, LoanApplicationStatus loanStatus);
}

package com.bank.repositories;

import com.bank.entities.LoanApplication;
import com.bank.entities.User;
import com.bank.enums.LoanApplicationStatus;
import com.bank.enums.UserAccountType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanApplicationRepository extends JpaRepository <LoanApplication, String> {
    boolean existsByMemberIdAndLoanApplicationStatus(String memberId, LoanApplicationStatus loanStatus);
    Optional<LoanApplication> findById(String loanApplicationId);
}

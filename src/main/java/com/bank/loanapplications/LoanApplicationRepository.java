package com.bank.loanapplications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanApplicationRepository extends JpaRepository <LoanApplication, String> {
    boolean existsByMemberIdAndLoanApplicationStatus(String memberId, LoanApplicationStatus loanStatus);
    Optional<LoanApplication> findById(String loanApplicationId);
    Page<LoanApplication> findByLoanOfficerId(String id, PageRequest pageRequest);
    Optional<LoanApplication> findByMemberIdAndLoanApplicationStatus(String id, LoanApplicationStatus loanApplicationStatus);
}

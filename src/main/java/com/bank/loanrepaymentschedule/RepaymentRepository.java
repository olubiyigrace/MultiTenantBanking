package com.bank.loanrepaymentschedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepaymentRepository extends JpaRepository<LoanRepaymentSchedule, String> {
    Optional<LoanRepaymentSchedule> findTopByLoanApplicationIdOrderByDueDateAsc(String id);
}

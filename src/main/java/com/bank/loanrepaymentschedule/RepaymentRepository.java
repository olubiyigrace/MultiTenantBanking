package com.bank.loanrepaymentschedule;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RepaymentRepository extends JpaRepository<LoanRepaymentSchedule, String> {
    Optional<LoanRepaymentSchedule> findTopByLoanApplicationIdOrderByDueDateAsc(String id);

    @Query("""
        SELECT lrs
        FROM LoanRepaymentSchedule lrs
        JOIN lrs.loanApplication la
        WHERE la.loanOfficer.id = :loanOfficerId
        AND lrs.dueDate < CURRENT_DATE
        AND lrs.balanceRemaining > 0
        ORDER BY lrs.dueDate ASC
    """)
    Page<LoanRepaymentSchedule> findOverdueSchedulesByLoanOfficer(@Param("loanOfficerId") String loanOfficerId, Pageable pageable);
}


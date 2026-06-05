package com.bank.loanguarantors;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuarantorRepository extends JpaRepository<LoanGuarantor, String> {
    Optional<LoanGuarantor> findByGuarantorMemberIdAndGuarantorStatus(String guarantorMemberId, GuarantorStatus guarantorStatus);
    LoanGuarantor findByLoanApplicationId(String loanApplicationId);
    boolean existsByGuarantorMemberIdAndGuarantorStatus(String id, GuarantorStatus guarantorStatus);
}

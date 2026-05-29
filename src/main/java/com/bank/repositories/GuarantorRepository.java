package com.bank.repositories;

import com.bank.entities.LoanGuarantor;
import com.bank.enums.GuarantorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuarantorRepository extends JpaRepository<LoanGuarantor, String> {
    Optional<LoanGuarantor> findByGuarantorMemberIdAndGuarantorStatus(String guarantorMemberId, GuarantorStatus guarantorStatus);
}

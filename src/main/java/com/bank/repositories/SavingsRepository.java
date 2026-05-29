package com.bank.repositories;

import com.bank.entities.SavingsAccount;
import com.bank.enums.SavingsAccountType;
import com.bank.enums.SavingsStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsRepository extends JpaRepository<SavingsAccount, String> {
    boolean existsByMemberIdAndSavingsStatus(String memberId, SavingsStatus savingsStatus);
}

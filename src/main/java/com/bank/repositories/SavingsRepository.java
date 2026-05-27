package com.bank.repositories;

import com.bank.entities.SavingsAccount;
import com.bank.enums.SavingsAccountType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsRepository extends JpaRepository<SavingsAccount, String> {
    boolean existsByMemberIdAndSavingsAccountType(String id,SavingsAccountType savingsAccountType);
    boolean existsBySavingsAccountType(SavingsAccountType savingsAccountType);
}

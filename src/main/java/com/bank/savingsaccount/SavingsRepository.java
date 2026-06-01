package com.bank.savingsaccount;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SavingsRepository extends JpaRepository<SavingsAccount, String> {
    boolean existsByMemberIdAndSavingsStatus(String memberId, SavingsStatus savingsStatus);
    boolean existsByMemberIdAndSavingsStatusAndSavingsAccountType(String id, SavingsStatus savingsStatus, SavingsAccountType savingsAccountType);
    Optional<SavingsAccount> findByMemberId(String memberId);
}

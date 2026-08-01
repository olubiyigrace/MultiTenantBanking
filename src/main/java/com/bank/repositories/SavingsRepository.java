package com.bank.repositories;

import com.bank.entities.MemberProfile;
import com.bank.entities.SavingsAccount;
import com.bank.enums.SavingsAccountType;
import com.bank.enums.SavingsStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavingsRepository extends JpaRepository<SavingsAccount, String> {
    boolean existsByMemberIdAndSavingsStatus(String memberId, SavingsStatus savingsStatus);
    Optional<SavingsAccount> findByMemberId(String memberId);
    SavingsAccount findByMemberIdAndSavingsStatusAndSavingsAccountType(String id, SavingsStatus savingsStatus, SavingsAccountType savingsAccountType);
    List<SavingsAccount> findByMemberIdAndSavingsStatus(String id, SavingsStatus savingsStatus);
    List<SavingsAccount> findBySavingsStatus(SavingsStatus savingsStatus);
    Optional<SavingsAccount> findByMember(MemberProfile member);
    Optional<SavingsAccount> findByAccountNumber(String accountNumber);
    long countByInstitutionId(String id);

    Optional<SavingsAccount> findByMemberIdAndSavingsAccountType(String id, SavingsAccountType savingsAccountType);
}

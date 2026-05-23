package com.bank.repositories;

import com.bank.entities.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavingsRepository extends JpaRepository<SavingsAccount, String> {
}

package com.bank.repositories;

import com.bank.entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository <Transaction,String> {
    Optional<Transaction> findByInstitutionIdAndDepositSlipNumber(String id, String depositSlipNumber);
}

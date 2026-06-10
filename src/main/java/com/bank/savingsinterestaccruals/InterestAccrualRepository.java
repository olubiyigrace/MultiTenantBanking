package com.bank.savingsinterestaccruals;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface InterestAccrualRepository extends JpaRepository<SavingsInterestAccrual, String> {
    boolean existsBySavingsAccountIdAndPeriodStart(String id, LocalDate postingDate);
}

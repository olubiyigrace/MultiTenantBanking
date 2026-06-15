package com.bank.savingsinterestaccruals;

import com.bank.savingsaccount.*;
import com.bank.transactions.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InterestAccrualServiceImpl implements InterestAccrualService {

    private static final BigDecimal REGULAR_RATE =
            new BigDecimal("0.08").divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);

    private static final BigDecimal FIXED_RATE =
            new BigDecimal("0.10").divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);

//    private static final BigDecimal TARGET_RATE =
//            new BigDecimal("0.10").divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);

    private static final BigDecimal REGULAR_CAP =
            BigDecimal.valueOf(500_000);

    private final InterestAccrualRepository interestAccrualRepository;
    private final SavingsRepository savingsRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public void processDailyInterest() {

        LocalDate today = LocalDate.now();
        List<SavingsAccount> accounts = savingsRepository.findBySavingsStatus(SavingsStatus.ACTIVE);
        for (SavingsAccount account : accounts) {

            if (!isEligible(account)) continue;
            switch (account.getSavingsAccountType()) {
                case REGULAR -> processRegular(account, today);
                case FIXED -> processFixed(account, today);
//                case TARGET -> processTarget(account, today);
            }
        }
    }

    private boolean isEligible(SavingsAccount account) {
        return account.getSavingsStatus() == SavingsStatus.ACTIVE
                && account.getBalance() != null
                && account.getBalance().compareTo(BigDecimal.ZERO) > 0;
    }

    private void processRegular(SavingsAccount account, LocalDate date) {
        BigDecimal eligibleBalance = account.getBalance().min(REGULAR_CAP);
        BigDecimal interest = eligibleBalance.multiply(REGULAR_RATE).setScale(2, RoundingMode.HALF_UP);
        creditInterest(account, interest, date, TransactionType.INTEREST_CREDIT);
    }

    private void processFixed(SavingsAccount account, LocalDate date) {
        BigDecimal principal = account.getBalance();
        BigDecimal interest = principal
                .multiply(FIXED_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        creditInterest(account, interest, date, TransactionType.INTEREST_CREDIT);
    }

    // ---------------- TARGET SAVINGS (ACCRUE ONLY) ----------------
//    private void processTarget(SavingsAccount account, LocalDate date) {
//
//        BigDecimal interest = account.getBalance()
//                .multiply(TARGET_RATE)
//                .setScale(2, RoundingMode.HALF_UP);
//
//        account.setAccruedInterest(
//                safe(account.getAccruedInterest()).add(interest)
//        );
//
//        savingsRepository.save(account);
//        SavingsInterestAccrual accrual = SavingsInterestAccrual.builder()
//                .savingsAccount(account)
//                .periodStart(date)
//                .periodEnd(date)
//                .openingBalance(account.getBalance())
//                .interestAmount(interest)
//                .creditedAt(LocalDateTime.now())
//                .build();
//
//        interestAccrualRepository.save(accrual);
//    }

    private void creditInterest(SavingsAccount account, BigDecimal interest, LocalDate date, TransactionType type) {
        if (interest.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal openingBalance = account.getBalance();
        BigDecimal newBalance = openingBalance.add(interest);
        account.setBalance(newBalance);
        savingsRepository.save(account);

        SavingsInterestAccrual accrual = SavingsInterestAccrual.builder()
                .savingsAccount(account)
                .periodStart(date)
                .periodEnd(date)
                .openingBalance(openingBalance)
                .interestAmount(interest)
                .creditedAt(LocalDateTime.now())
                .build();
        interestAccrualRepository.save(accrual);
        createTransaction(account, openingBalance, interest, type);
    }

    private void createTransaction(SavingsAccount account, BigDecimal balance, BigDecimal interest, TransactionType type) {
        Transaction transaction = Transaction.builder()
                .savingsAccount(account)
                .transactionType(type)
                .amount(interest)
                .balanceBefore(balance)
                .balanceAfter(balance.add(interest))
                .description("Daily interest credit")
                .reference("INT-" + System.currentTimeMillis())
                .transactionStatus(TransactionStatus.COMPLETED)
                .build();
        transactionRepository.save(transaction);
    }
}
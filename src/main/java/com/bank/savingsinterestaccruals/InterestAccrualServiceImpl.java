package com.bank.savingsinterestaccruals;

import com.bank.savingsaccount.SavingsAccount;
import com.bank.savingsaccount.SavingsAccountType;
import com.bank.savingsaccount.SavingsRepository;
import com.bank.savingsaccount.SavingsStatus;
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
public class InterestAccrualServiceImpl implements InterestAccrualService{
    private final InterestAccrualRepository interestAccrualRepository;
    private final SavingsRepository savingsRepository;
//    private final TransactionRepository transactionRepository;

    @Override
    public void processDailyInterest() {
        LocalDate today = LocalDate.now();
        List<SavingsAccount> accounts = savingsRepository.findBySavingsStatus(SavingsStatus.ACTIVE);

        for (SavingsAccount account : accounts) {
            if (!isEligibleForInterest(account)) {
                continue;
            }
            accrueInterest(account, today);
        }
    }

    private void accrueInterest(SavingsAccount account, LocalDate postingDate) {
        boolean alreadyProcessed = interestAccrualRepository.existsBySavingsAccountIdAndPeriodStart(account.getId(), postingDate);
        if (alreadyProcessed) {
            return;
        }
        BigDecimal dailyRate = account.getInterestRatePercent().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal openingBalance = account.getBalance();
        BigDecimal interestAmount = openingBalance.multiply(dailyRate).setScale(2, RoundingMode.HALF_UP);

        if (interestAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        account.setBalance(openingBalance.add(interestAmount));
        savingsRepository.save(account);

        SavingsInterestAccrual accrual = SavingsInterestAccrual.builder()
                .savingsAccount(account)
                .periodStart(postingDate)
                .periodEnd(postingDate)
                .openingBalance(openingBalance)
                .interestAmount(interestAmount)
                .creditedAt(LocalDateTime.now())
                .build();
        interestAccrualRepository.save(accrual);
    }
//        createInterestTransaction(account, openingBalance, interestAmount);

    private boolean isEligibleForInterest(SavingsAccount account) {
        if (account.getSavingsStatus() != SavingsStatus.ACTIVE) {
            return false;
        }

        if (account.getSavingsAccountType()
                == SavingsAccountType.REGULAR) {
            return false;
        }

        if (account.getSavingsAccountType()
                == SavingsAccountType.FIXED
                && account.getMaturityDate() != null
                && LocalDate.now().isAfter(account.getMaturityDate())) {
            return false;
        }
        return true;
    }
//
//    private void createInterestTransaction(SavingsAccount account, BigDecimal openingBalance, BigDecimal interestAmount) {
//        SavingsTransaction transaction = SavingsTransaction.builder()
//                        .savingsAccount(account)
//                        .transactionType(TransactionType.INTEREST_ACCRUAL)
//                        .amount(interestAmount)
//                        .balanceBefore(openingBalance)
//                        .balanceAfter(openingBalance.add(interestAmount))
//                        .narration("Daily interest accrual")
//                        .transactionDate(LocalDateTime.now())
//                        .build();
//
//        transactionRepository.save(transaction);
//    }
}

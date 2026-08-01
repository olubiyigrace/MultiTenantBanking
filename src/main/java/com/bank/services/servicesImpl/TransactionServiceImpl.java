package com.bank.services.servicesImpl;

import com.bank.dtos.requestDtos.DepositRequest;
import com.bank.entities.Transaction;
import com.bank.enums.TransactionStatus;
import com.bank.enums.TransactionType;
import com.bank.exceptions.DuplicateResourceException;
import com.bank.exceptions.InvalidRequestException;
import com.bank.services.EmailService;
import com.bank.entities.SavingsAccount;
import com.bank.services.TransactionService;
import com.bank.utils.CurrencyUtil;
import com.bank.repositories.TransactionRepository;
import com.bank.enums.SavingsAccountType;
import com.bank.repositories.SavingsRepository;
import com.bank.enums.SavingsStatus;
import com.bank.entities.User;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final SavingsRepository savingsRepository;
    private final EmailService emailService;

    @Override
    public void createDeposit(DepositRequest depositRequest) {
        if (!depositRequest.getTransactionType().equals(TransactionType.DEPOSIT)) {
            throw new InvalidRequestException("only deposits are allowed");
        }

        SavingsAccount savingsAccount = savingsRepository.findByAccountNumber(depositRequest.getAccountNumber())
                .orElseThrow(() -> new InvalidRequestException("Invalid account number"));
        if (!savingsAccount.getSavingsAccountType().equals(SavingsAccountType.REGULAR)) {
            throw new InvalidRequestException("Deposits can only be made into a regular account");
        }
        if (!savingsAccount.getSavingsStatus().equals(SavingsStatus.ACTIVE)) {
            throw new InvalidRequestException("Deposit can only be made into an active account");
        }

        User accountHolder = savingsAccount.getMember().getUser();
        if (!accountHolder.getName().equalsIgnoreCase(depositRequest.getName())) {
            throw new InvalidRequestException("Provided name does not match the account holder");
        }

        Optional<Transaction> existingTransaction = transactionRepository.findByInstitutionIdAndDepositSlipNumber(savingsAccount.getInstitution().getId(), depositRequest.getDepositSlipNumber());
        if(existingTransaction.isPresent()){
            throw new DuplicateResourceException("Transaction is already " + existingTransaction.get().getTransactionStatus() + ". Duplicate transactions are not allowed");
        }

        Transaction transaction = Transaction.builder()
                .amount(depositRequest.getAmount())
                .description(depositRequest.getDescription())
                .depositSlipNumber(depositRequest.getDepositSlipNumber())
                .transactionType(depositRequest.getTransactionType())
                .build();

        transaction.setInstitution(savingsAccount.getInstitution());
        transaction.setSavingsAccount(savingsAccount);
        transaction.setUser(accountHolder);
        transaction.setBalanceBefore(savingsAccount.getBalance());
        savingsAccount.setBalance((savingsAccount.getBalance()).add(depositRequest.getAmount()));
        transaction.setBalanceAfter(savingsAccount.getBalance());
        transaction.setIdempotencyKey("DEP: " + depositRequest.getDepositSlipNumber());
        transaction.setReference("TXN-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + savingsAccount.getInstitution().getId().toUpperCase().replace("-", "")
                + UUID.randomUUID().toString().toUpperCase().replace("-", ""));
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);
        savingsRepository.save(savingsAccount);
        transactionRepository.save(transaction);

        Map<String, Object> model = new HashMap<>();

        String accountNumber = savingsAccount.getAccountNumber();
        String maskedAccountNumber = accountNumber.substring(0, 2)
                + "*".repeat(accountNumber.length() - 4)
                + accountNumber.substring(accountNumber.length() - 2);

        String transactionDate = formatDateTime(transaction.getCreatedAt());

        model.put("name", accountHolder.getName());
        model.put("amount", CurrencyUtil.naira(transaction.getAmount()));
        model.put("newBalance", CurrencyUtil.naira(transaction.getBalanceAfter()));
        model.put("institutionName", savingsAccount.getInstitution().getInstitutionName());
        model.put("accountNumber", maskedAccountNumber);
        model.put("transactionReference", transaction.getReference());
        model.put("transactionDate", transactionDate);
        model.put("currentYear", Year.now().getValue());
        model.put("description", transaction.getDescription());
        model.put("institutionPhone", savingsAccount.getInstitution().getInstitutionPhone());
        model.put("institutionEmail", savingsAccount.getInstitution().getInstitutionEmail());
        model.put("rcNumber", savingsAccount.getInstitution().getInstitutionRcNumber());
        try {
            emailService.sendVerificationEmail(
                    accountHolder.getEmail(),
                    "Deposit Successful",
                    "deposit",
                    model
            );
        } catch (UnsupportedEncodingException | MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        int day = dateTime.getDayOfMonth();

        String suffix;
        if (day >= 11 && day <= 13) {
            suffix = "th";
        } else {
            switch (day % 10) {
                case 1 -> suffix = "st";
                case 2 -> suffix = "nd";
                case 3 -> suffix = "rd";
                default -> suffix = "th";
            }
        }
        return day + suffix + " " + dateTime.format(DateTimeFormatter.ofPattern("MMMM, yyyy 'at' hh:mma"));
    }

//    @Override
//    public void reverseTransaction(ReversalRequest reversalRequest){
//
//    }
}



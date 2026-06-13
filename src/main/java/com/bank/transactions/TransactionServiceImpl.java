package com.bank.transactions;

import com.bank.others.exceptions.InvalidRequestException;
import com.bank.others.services.EmailService;
import com.bank.savingsaccount.SavingsAccount;
import com.bank.savingsaccount.SavingsAccountType;
import com.bank.savingsaccount.SavingsRepository;
import com.bank.savingsaccount.SavingsStatus;
import com.bank.users.User;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TransactionServiceImpl implements TransactionService{
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final SavingsRepository savingsRepository;
    private  final EmailService emailService;

    @Override
    public void createDeposit(TransactionRequest transactionRequest) throws MessagingException {
        if(!transactionRequest.getTransactionType().equals(TransactionType.DEPOSIT)){
            throw new InvalidRequestException("only deposits are allowed");
        }

        SavingsAccount savingsAccount = savingsRepository.findByAccountNumber(transactionRequest.getAccountNumber())
                .orElseThrow(() -> new InvalidRequestException("Invalid account number"));
        if (!savingsAccount.getSavingsAccountType().equals(SavingsAccountType.REGULAR)){
           throw new InvalidRequestException("Deposits can only be made into a regular account");
        }
        if (!savingsAccount.getSavingsStatus().equals(SavingsStatus.ACTIVE)){
            throw new InvalidRequestException("Deposit can only be made into an active account");
        }

        User accountHolder = savingsAccount.getMember().getUser();
        if (!accountHolder.getName().equalsIgnoreCase(transactionRequest.getName())) {
            throw new InvalidRequestException("Provided name does not match the account holder");
        }

        Transaction transaction = transactionMapper.toEntity(transactionRequest);
        transaction.setInstitution(savingsAccount.getInstitution());
        transaction.setSavingsAccount(savingsAccount);
        transaction.setUser(accountHolder);
        transaction.setBalanceBefore(savingsAccount.getBalance());
        savingsAccount.setBalance((savingsAccount.getBalance()).add(transactionRequest.getAmount()));
        transaction.setBalanceAfter(savingsAccount.getBalance());
        transaction.setReference("TXN-" + LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + savingsAccount.getInstitution().getId().replace("-", "")
                + UUID.randomUUID().toString().replace("-", ""));
        transaction.setTransactionStatus(TransactionStatus.COMPLETED);

        savingsRepository.save(savingsAccount);
        transactionRepository.save(transaction);

        Map<String, Object> model = new HashMap<>();
        model.put("name", accountHolder.getName());
        model.put("amount", CurrencyUtil.naira(transaction.getAmount()));
        model.put("newBalance", CurrencyUtil.naira(transaction.getBalanceAfter()));
        model.put("institutionName", savingsAccount.getInstitution().getInstitutionName());
        model.put("accountNumber", savingsAccount.getAccountNumber());
        model.put("transactionReference", transaction.getReference());
        model.put("transactionDate", transaction.getCreatedAt());
        model.put("currentYear", Year.now().getValue());
        model.put("institutionPhone", savingsAccount.getInstitution().getInstitutionPhone());
        model.put("institutionEmail", savingsAccount.getInstitution().getInstitutionEmail());
        model.put("rcNumber", savingsAccount.getInstitution().getInstitutionRcNumber());

        emailService.sendVerificationEmail(
                accountHolder.getEmail(),
                "Deposit Successful",
                "deposit",
                model
        );
    }
}

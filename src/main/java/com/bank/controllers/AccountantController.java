package com.bank.controllers;

import com.bank.others.utils.ApiResponse;
import com.bank.loanapplications.LoanApplicationService;
import com.bank.transactions.TransactionRequest;
import com.bank.transactions.TransactionService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
@PreAuthorize("hasRole('ACCOUNTANT')")
public class AccountantController {
    private final LoanApplicationService loanApplicationService;
    private final TransactionService transactionService;


    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<String>> deposit(@RequestBody TransactionRequest transactionRequest) throws MessagingException {
        transactionService.createDeposit(transactionRequest);
        return ResponseEntity.ok(ApiResponse.success(true, "Deposit successful", null));
    }
    @PostMapping("/disburse-loan")
    public ResponseEntity<ApiResponse<String>> disburse(@RequestParam String loanApplicationId){
        loanApplicationService.disburseLoan(loanApplicationId);
        return ResponseEntity.ok(ApiResponse.success(true, "Loan disbursed successfully", null));
    }
}

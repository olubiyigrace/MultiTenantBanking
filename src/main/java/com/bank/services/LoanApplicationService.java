package com.bank.services;

import com.bank.requests.LoanApplicationRequest;
import com.bank.responses.LoanApplicationResponse;
import com.bank.responses.PageResponse;

public interface LoanApplicationService {
    void createApplication(LoanApplicationRequest loanApplicationRequest);
    PageResponse<LoanApplicationResponse> getAllApplications(int page, int size);
    void reviewLoan(String loanApplicationId);

    void reviewLoanApplication(String loanApplicationId);

    void approveLoan(String loanApplicationId);
    void rejectLoan(String loanApplicationId);
    void disburseLoan(String loanApplicationId);
    void checkIfRepaid(String loanApplicationId);
    void addDefaulter(String loanApplicationId);
    void writeOff(String loanApplicationId);
}

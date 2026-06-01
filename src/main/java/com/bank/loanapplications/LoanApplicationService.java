package com.bank.loanapplications;

import com.bank.others.utils.PageResponse;

public interface LoanApplicationService {
    void createApplication(LoanApplicationRequest loanApplicationRequest);
    PageResponse<LoanApplicationResponse> getAllApplications(int page, int size);
    void reviewLoanApplication(String loanApplicationId);
    void assignApplication(String loanApplicationId, String loanOfficerId);
    PageResponse<LoanApplicationResponse> getAllAssignedApplications(int page, int size);
    void approveLoan(String loanApplicationId);
    void rejectLoan(String loanApplicationId, LoanRejectionRequest loanRejectionRequest);
    void disburseLoan(String loanApplicationId);
    void checkIfRepaid(String loanApplicationId);
    void addDefaulter(String loanApplicationId);
    void writeOff(String loanApplicationId);
}

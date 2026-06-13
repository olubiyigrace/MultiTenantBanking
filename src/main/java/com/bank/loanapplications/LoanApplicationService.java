package com.bank.loanapplications;

import com.bank.loanproducts.LoanProductResponse;
import com.bank.loanrepaymentschedule.OverdueRepaymentScheduleResponse;
import com.bank.others.utils.PageResponse;

import java.util.List;

public interface LoanApplicationService {
    List<LoanProductResponse> getEligibleLoanProducts();
    void createApplication(LoanApplicationRequest loanApplicationRequest);
    PageResponse<LoanApplicationResponse> getAllApplications(int page, int size);
    void reviewLoanApplication(String loanApplicationId);
    void assignApplication(String loanApplicationId, String loanOfficerId);
    PageResponse<LoanApplicationResponse> getAllAssignedApplications(int page, int size);
    void recommendApproval(String loanApplicationId);
    void approveLoan(String loanApplicationId);
    void recommendRejection(String loanApplicationId);
    void rejectLoan(String loanApplicationId, LoanRejectionRequest loanRejectionRequest);
    void disburseLoan(String loanApplicationId);
    void checkIfRepaid(String loanApplicationId);
    void addDefaulter(String loanApplicationId);
    void writeOff(String loanApplicationId);
    PageResponse<OverdueRepaymentScheduleResponse> getOverdueRepaymentSchedules(int page, int size);
}

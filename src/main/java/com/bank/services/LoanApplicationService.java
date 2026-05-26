package com.bank.services;

import com.bank.requests.LoanApplicationRequest;

public interface LoanApplicationService {
    void createApplication(LoanApplicationRequest loanApplicationRequest);
}

package com.bank.services;

import com.bank.dtos.requestDtos.LoanProductRequest;
import com.bank.dtos.responseDtos.LoanProductResponse;
import com.bank.utils.PageResponse;

public interface LoanProductService {
    void create(LoanProductRequest loanProductRequest);
    LoanProductResponse getSingle(String id);
    PageResponse<LoanProductResponse> getAll(final int page, final int size);
    void update(String id, LoanProductRequest loanProductRequest);
    void delete(String id);
    void activateLoanProduct(String loanProductId);
    void deactivateLoanProduct(String loanProductId);
}

package com.bank.services;

import com.bank.common.PageResponse;
import com.bank.requests.LoanProductRequest;
import com.bank.responses.LoanProductResponse;


public interface LoanProductService {
    void create(LoanProductRequest loanProductRequest);
    LoanProductResponse getSingle(String id);
    PageResponse<LoanProductResponse> getAll(final int page, final int size);
    void update(String id, LoanProductRequest loanProductRequest);
    void delete(String id);
}

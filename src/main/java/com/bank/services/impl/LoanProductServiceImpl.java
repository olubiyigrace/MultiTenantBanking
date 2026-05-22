package com.bank.services.impl;

import com.bank.repositories.LoanProductRepository;
import com.bank.requests.LoanProductRequest;
import com.bank.responses.LoanProductResponse;
import com.bank.services.LoanProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanProductServiceImpl implements LoanProductService {
    private final LoanProductRepository loanProductRepository;

    @Override
    public void create(LoanProductRequest loanProductRequest) {

    }

    @Override
    public LoanProductResponse getSingle(String id) {
        return null;
    }

    @Override
    public List<LoanProductResponse> getAll() {
        return List.of();
    }

    @Override
    public void update(String id, LoanProductRequest loanProductRequest) {

    }

    @Override
    public void delete(String id) {

    }
}

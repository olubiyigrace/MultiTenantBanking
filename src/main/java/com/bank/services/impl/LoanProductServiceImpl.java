package com.bank.services.impl;

import com.bank.common.PageResponse;
import com.bank.entities.LoanProduct;
import com.bank.mapper.LoanProductMapper;
import com.bank.repositories.LoanProductRepository;
import com.bank.requests.LoanProductRequest;
import com.bank.responses.LoanProductResponse;
import com.bank.services.LoanProductService;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class LoanProductServiceImpl implements LoanProductService {
    private final LoanProductRepository loanProductRepository;
    private final LoanProductMapper loanProductMapper;

    @Override
    public void create(LoanProductRequest loanProductRequest) {
        Optional<LoanProduct> existingLoanProduct = loanProductRepository.findLoanProductByName(loanProductRequest.getName());
        if (existingLoanProduct.isPresent()){
            log.debug("Loan product already exists");
            throw new DuplicateRequestException("Loan product already exist");
        }
        LoanProduct newLoanProduct = loanProductMapper.toEntity(loanProductRequest);
        loanProductRepository.save(newLoanProduct);
    }

    @Override
    public LoanProductResponse getSingle(String id) {
        return null;
    }

    @Override
    public PageResponse<LoanProductResponse> getAll(int page, int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<LoanProduct> institutions = loanProductRepository.findAll(pageRequest);
        final Page<LoanProductResponse> loanProductResponse = institutions.map(loanProductMapper::toResponse);
        return PageResponse.of(loanProductResponse);
    }


    @Override
    public void update(String id, LoanProductRequest loanProductRequest) {

    }

    @Override
    public void delete(String id) {

    }
}

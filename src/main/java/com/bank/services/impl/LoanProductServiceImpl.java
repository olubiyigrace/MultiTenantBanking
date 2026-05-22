package com.bank.services.impl;

import com.bank.common.PageResponse;
import com.bank.entities.LoanProduct;
import com.bank.exceptions.InvalidRequestException;
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
            throw new DuplicateRequestException("Loan product already exists");
        }
        if (loanProductRequest.getMinAmount() != null && loanProductRequest.getMaxAmount() != null
                && loanProductRequest.getMaxAmount().compareTo(loanProductRequest.getMinAmount()) < 0) {
            log.debug("Maximum amount cannot be less than minimum amount");
            throw new InvalidRequestException("Maximum amount cannot be less than minimum amount");
        }
        LoanProduct newLoanProduct = loanProductMapper.toEntity(loanProductRequest);
        loanProductRepository.save(newLoanProduct);
        log.debug("Loan product created");
    }

    @Override
    public LoanProductResponse getSingle(String id) {
        Optional<LoanProduct> existingLoanProduct = loanProductRepository.findById(id);
        if (existingLoanProduct.isEmpty()){
            log.debug("Loan product does not exist");
            throw new DuplicateRequestException("Loan product does not exist");
        }
        LoanProduct foundLoanProduct = existingLoanProduct.get();
        return loanProductMapper.toResponse(foundLoanProduct);
    }

    @Override
    public PageResponse<LoanProductResponse> getAll(int page, int size) {
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<LoanProduct> loanProducts = loanProductRepository.findAll(pageRequest);
        final Page<LoanProductResponse> loanProductResponse = loanProducts.map(loanProductMapper::toResponse);
        return PageResponse.of(loanProductResponse);
    }


    @Override
    public void update(String id, LoanProductRequest loanProductRequest) {
        Optional<LoanProduct> existingLoanProduct = loanProductRepository.findById(id);
        if (existingLoanProduct.isEmpty()){
            throw new DuplicateRequestException("Loan product does not exist");
        }
        LoanProduct foundLoanProduct = existingLoanProduct.get();
        loanProductRepository.save(foundLoanProduct);
        log.debug("Loan product updated");
    }

    @Override
    public void delete(String id) {
        Optional<LoanProduct> existingLoanProduct = loanProductRepository.findById(id);
        if (existingLoanProduct.isEmpty()) {
            log.debug("Loan product not found");
            throw new DuplicateRequestException("Loan product does not exist");
        }
            LoanProduct foundLoanProduct = existingLoanProduct.get();
            loanProductRepository.delete(foundLoanProduct);
            log.debug("Loan product deleted");
    }
}

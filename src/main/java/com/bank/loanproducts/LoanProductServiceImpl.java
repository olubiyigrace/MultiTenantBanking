package com.bank.loanproducts;

import com.bank.others.utils.CurrentUserUtil;
import com.bank.others.utils.PageResponse;
import com.bank.others.config.InstitutionContext;
import com.bank.institutions.Institution;
import com.bank.others.exceptions.InvalidRequestException;
import com.bank.users.User;
import com.sun.jdi.request.DuplicateRequestException;
import jakarta.persistence.EntityNotFoundException;
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
    private final CurrentUserUtil currentUserUtil;

    @Override
    public void create(LoanProductRequest loanProductRequest) {
        final String institutionId = InstitutionContext.getCurrentInstitution();
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
        newLoanProduct.setInstitution(Institution.builder().id(institutionId).build());
        newLoanProduct.setIsActive(true);
        loanProductRepository.save(newLoanProduct);
        log.debug("Loan product created");
    }

    @Override
    public LoanProductResponse getSingle(String id) {
        Optional<LoanProduct> existingLoanProduct = loanProductRepository.findById(id);
        if (existingLoanProduct.isEmpty()){
            log.debug("Loan product does not exist");
            throw new InvalidRequestException("Loan product does not exist");
        }
        LoanProduct foundLoanProduct = existingLoanProduct.get();
        return loanProductMapper.toResponse(foundLoanProduct);
    }

    @Override
    public PageResponse<LoanProductResponse> getAll(int page, int size) {
        User loggedInUser = currentUserUtil.getLoggedInUser();
        final PageRequest pageRequest = PageRequest.of(page, size);
        final Page<LoanProduct> loanProducts = loanProductRepository.findByInstitution(loggedInUser.getInstitution(), pageRequest);
        final Page<LoanProductResponse> loanProductResponse = loanProducts.map(loanProductMapper::toResponse);
        return PageResponse.of(loanProductResponse);
    }


    @Override
    public void update(String id, LoanProductRequest loanProductRequest) {
        Optional<LoanProduct> existingLoanProduct = loanProductRepository.findById(id);
        if (existingLoanProduct.isEmpty()){
            throw new InvalidRequestException("Loan product does not exist");
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
            throw new InvalidRequestException("Loan product does not exist");
        }
            LoanProduct foundLoanProduct = existingLoanProduct.get();
            loanProductRepository.delete(foundLoanProduct);
            log.debug("Loan product deleted");
    }
    @Override
    public void activateLoanProduct(String loanProductId) {
        final LoanProduct loanProduct = loanProductRepository.findById(loanProductId)
                .orElseThrow(() -> new EntityNotFoundException("Loan product with the id '" + loanProductId + "' does not exist"));
        if (loanProduct.getIsActive().equals(true)) {
            log.debug("Loan product has already been activated");
            throw new DuplicateRequestException("Loan product has already been activated");
        }
       loanProduct.setIsActive(true);
        loanProductRepository.save(loanProduct);
    }

    @Override
    public void deactivateLoanProduct(String loanProductId) {
        final LoanProduct loanProduct = loanProductRepository.findById(loanProductId)
                .orElseThrow(() -> new EntityNotFoundException("Loan product with the id '" + loanProductId + "' does not exist"));
        if (loanProduct.getIsActive().equals(false)) {
            log.debug("Loan product has already been deactivated");
            throw new DuplicateRequestException("Loan product has already been deactivated");
        }
        loanProduct.setIsActive(false);
        loanProductRepository.save(loanProduct);
    }
}

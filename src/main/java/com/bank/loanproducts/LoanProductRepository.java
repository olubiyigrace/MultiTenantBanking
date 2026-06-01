package com.bank.loanproducts;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanProductRepository extends JpaRepository<LoanProduct, String> {
    Optional<LoanProduct> findLoanProductByName(String name);
    boolean existsByInstitutionIdAndRequiresGuarantor(String institutionId, boolean requiresGuarantor);
    boolean existsByInstitutionIdAndRequiresCollateral(String institutionId, boolean requiresCollateral);
}

package com.bank.repositories;

import com.bank.entities.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanProductRepository extends JpaRepository<LoanProduct, String> {
    Optional<LoanProduct> findLoanProductByName(String name);
    boolean existsByInstitutionIdAndRequiresGuarantorIs(String institutionId, boolean requiresGuarantor);
}

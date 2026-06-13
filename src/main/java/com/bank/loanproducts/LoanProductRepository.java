package com.bank.loanproducts;

import com.bank.institutions.Institution;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanProductRepository extends JpaRepository<LoanProduct, String> {
    Optional<LoanProduct> findLoanProductByName(String name);
    boolean existsByInstitutionIdAndRequiresCollateral(String institutionId, boolean requiresCollateral);
    Page<LoanProduct> findByInstitution(Institution institution, PageRequest pageRequest);
    Optional<LoanProduct> findByIdAndInstitutionId(String loanProductId, String institutionId);
    Optional<LoanProduct> findByInstitutionIdAndIsActiveTrue(String institutionId);
}

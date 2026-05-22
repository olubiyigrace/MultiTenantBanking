package com.bank.repositories;

import com.bank.entities.LoanProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanProductRepository extends JpaRepository<LoanProduct, String> {
}

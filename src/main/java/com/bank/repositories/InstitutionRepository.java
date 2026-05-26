package com.bank.repositories;

import com.bank.entities.Institution;
import com.bank.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, String> {
    boolean existsByInstitutionEmail(String email);
    boolean existsByInstitutionRcNumber(String institutionRcNumber);
    Optional<Institution> findByInstitutionEmail(String email);
    Optional<Institution> findById(String institutionId);
}

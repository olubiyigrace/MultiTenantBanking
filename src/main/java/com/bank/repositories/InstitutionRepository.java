package com.bank.repositories;

import com.bank.entities.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InstitutionRepository extends JpaRepository<Institution, String> {
    boolean existsByEmail(String email);
    boolean existsByRcNumber(String rcNumber);
    Optional<Institution> findByEmail(String email);
    Optional<Institution> findByAdminEmail(String AdminEmail);
}

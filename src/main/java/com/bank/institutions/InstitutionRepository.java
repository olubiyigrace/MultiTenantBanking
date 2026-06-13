package com.bank.institutions;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, String> {
    boolean existsByInstitutionEmail(String email);
    boolean existsByInstitutionRcNumber(String institutionRcNumber);
    Optional<Institution> findByInstitutionEmail(String email);
    Optional<Institution> findById(String institutionId);

    @Query(value = """
    UPDATE institutions
    SET next_member_sequence = next_member_sequence + 1
    WHERE id = :institutionId
    RETURNING next_member_sequence
""", nativeQuery = true)
    Long getNextMemberSequence(@Param("institutionId") String institutionId);
}

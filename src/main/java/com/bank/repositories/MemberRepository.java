package com.bank.repositories;

import com.bank.entities.Institution;
import com.bank.entities.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberProfile, String> {
    boolean existsByMemberNumberAndInstitution(String memberNumber, Institution institution);
    Optional<MemberProfile> findMemberProfileByBvn(String bvn);

    @Query(value = "SELECT * FROM members WHERE institution_id = :institutionId ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    Optional<MemberProfile> findLastMemberByInstitution(String institutionId);
}

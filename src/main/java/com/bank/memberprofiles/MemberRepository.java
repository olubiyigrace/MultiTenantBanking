package com.bank.memberprofiles;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberProfile, String> {
    Optional<MemberProfile> findTopByOrderByCreatedAtDesc();
    Optional<MemberProfile> findByBvn(String bvn);
    Optional<MemberProfile> findById(String memberId);
    Optional<MemberProfile> findByUserId(String id);

    @Query("SELECT m FROM MemberProfile m JOIN FETCH m.user WHERE m.profileStatus = :profileStatus")
    Page<MemberProfile> findByProfileStatus(ProfileStatus profileStatus, Pageable pageable);
}

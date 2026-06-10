package com.bank.memberprofiles;

import com.bank.institutions.Institution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberProfile, String> {
    Optional<MemberProfile> findTopByOrderByCreatedAtDesc();
    Optional<MemberProfile> findById(String memberId);
    List<MemberProfile> findByUserId(String id);
    Page<MemberProfile> findByInstitutionAndProfileStatus(Institution institution, ProfileStatus profileStatus, PageRequest pageRequest);
    Optional<MemberProfile> findByBvnAndInstitutionId(String bvn, String institutionId);
    Optional<MemberProfile> findByUserIdAndInstitutionId(String id, String institutionId);
}

package com.bank.repositories;

import com.bank.entities.MemberProfile;
import com.bank.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberProfile, String> {
    Optional<MemberProfile> findTopByOrderByCreatedAtDesc();
    boolean existsByUser(User user);

}

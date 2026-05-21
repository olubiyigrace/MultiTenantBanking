package com.bank.repositories;

import com.bank.entities.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<MemberProfile, String> {
}

package com.bank.repositories;

import com.bank.entities.Otp;
import com.bank.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email, OtpPurpose purpose);
    Optional<Otp> findByEmailAndPurpose(String email, OtpPurpose otpPurpose);
}

package com.bank.auth.repository;

import com.bank.auth.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    Optional<UserSession> findByAccessToken(String accessToken);
}

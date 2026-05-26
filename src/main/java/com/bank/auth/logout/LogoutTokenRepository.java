package com.bank.auth.logout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface LogoutTokenRepository extends JpaRepository<LogoutToken, String> {
    Optional<LogoutToken> findByToken(String token);
}

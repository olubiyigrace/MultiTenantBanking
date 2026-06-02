package com.bank.others.logout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface LogoutTokenRepository extends JpaRepository<LogoutToken, String> {
}

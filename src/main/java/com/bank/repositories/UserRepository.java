package com.bank.repositories;

import com.bank.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, String> {
    Boolean existsByEmail(String email);
}

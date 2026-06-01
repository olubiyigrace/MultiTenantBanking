package com.bank.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>{
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Optional<User> findFirstByInstitution_IdAndUserAccountType(String institutionId, UserAccountType userAccountType);
}

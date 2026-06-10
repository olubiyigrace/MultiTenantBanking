package com.bank.users;

import com.bank.institutions.Institution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>{
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Page<User> findByInstitution(Institution institution, PageRequest pageRequest);
    Optional<User> findByEmailAndInstitution_Id(String email, String institutionId);
    boolean existsByEmail(String email);
}

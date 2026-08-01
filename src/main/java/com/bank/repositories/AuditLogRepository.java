package com.bank.repositories;

import com.bank.entities.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, String> {
}

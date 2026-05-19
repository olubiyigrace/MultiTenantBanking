package com.bank.entities;

import com.bank.common.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder

@Table(name = "audit_logs")
public class AuditLog extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institution;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    private String entityType;
    private String action;
    private UUID entityId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
}

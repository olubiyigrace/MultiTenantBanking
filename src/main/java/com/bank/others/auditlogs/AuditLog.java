package com.bank.others.auditlogs;

import com.bank.others.entity.AbstractEntity;
import com.bank.institutions.Institution;
import com.bank.users.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder

@Table(name = "audit_logs")
public class AuditLog extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", foreignKey = @ForeignKey(name = "fk_audit_log_institution_id"))
    private Institution institution;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_audit_log_user_id"))
    private User user;

    private String entityType;
    private String action;
    private String entityId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
}

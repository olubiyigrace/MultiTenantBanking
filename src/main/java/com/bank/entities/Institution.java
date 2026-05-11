package com.bank.entities;

import com.bank.common.AbstractEntity;
import com.bank.enums.InstitutionStatus;
import com.bank.enums.InstitutionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "institutions")
public class Institution extends AbstractEntity {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(unique = true, nullable = false)
    private String rcNumber;

    @Column(nullable = false, unique = true)
    private String email;

    @Builder.Default
    private String baseCurrency = "NGN";

    @Enumerated(EnumType.STRING)
    private InstitutionType institutionType;

    @Enumerated(EnumType.STRING)
    private InstitutionStatus status = InstitutionStatus.PENDING;

    @Column(nullable = false)
    private String adminName;

    @Column(nullable = false)
    private String adminEmail;

    @Column(nullable = false)
    private String adminPhone;

    @Column(nullable = false)
    private String adminNin;

    @Column(nullable = false)
    private String adminPassword;

    private Boolean isVerified = false;
    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;
    private LocalDateTime emailVerifiedAt;
}

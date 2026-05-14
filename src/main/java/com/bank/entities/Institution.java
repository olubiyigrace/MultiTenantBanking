package com.bank.entities;

import com.bank.common.AbstractEntity;
import com.bank.enums.InstitutionStatus;
import com.bank.enums.InstitutionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "institutions")
public class Institution extends AbstractEntity {

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String companyPhone;

    @Column(unique = true, nullable = false)
    private String companyRcNumber;

    @Column(nullable = false, unique = true)
    private String companyEmail;

    @Builder.Default
    private String baseCurrency = "NGN";

    @Enumerated(STRING)
    private InstitutionType institutionType;

    @Enumerated(STRING)
    private InstitutionStatus institutionStatus = InstitutionStatus.PENDING;

    @Column(nullable = false)
    private String adminName;

    @Column(nullable = false)
    private String adminUsername;

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

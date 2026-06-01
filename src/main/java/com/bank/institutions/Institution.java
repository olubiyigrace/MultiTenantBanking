package com.bank.institutions;

import com.bank.others.AbstractEntity;
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
    private String institutionName;

    @Column(nullable = false)
    private String institutionPhone;

    @Column(unique = true, nullable = false)
    private String institutionRcNumber;

    @Column(nullable = false, unique = true)
    private String institutionEmail;

    @Builder.Default
    private String baseCurrency = "NGN";

    @Enumerated(EnumType.STRING)
    private InstitutionType institutionType;

    @Enumerated(EnumType.STRING)
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

    @Builder.Default
    private Boolean isVerified = false;

    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;
    private LocalDateTime emailVerifiedAt;
}

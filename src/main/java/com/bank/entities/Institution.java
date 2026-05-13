package com.bank.entities;

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
public class Institution {
    @Id
    @GeneratedValue(strategy = UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private String id;

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

    @Enumerated(STRING)
    private InstitutionType institutionType;

    @Enumerated(STRING)
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

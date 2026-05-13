package com.bank.entities;

import com.bank.common.AbstractEntity;
import com.bank.enums.UserAccountType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "users")
public class User extends AbstractEntity {
    @Column(nullable = false, updatable = false)
    private String name;

    @Column(nullable = false, updatable = true)
    private String password;

    @Column(nullable = false, updatable = false)
    private String phone;

    @Column(nullable = false, updatable = false)
    private String nin;

    @Column(unique = true, nullable = false, updatable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private UserAccountType userAccountType;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Institution institution;
}

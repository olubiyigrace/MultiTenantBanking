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
    private String name;
    private String password;
    private String phone;
    private String nin;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private UserAccountType accountType;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", foreignKey = @ForeignKey(name = "fk_user_institution_id"))
    private Institution institution;
}

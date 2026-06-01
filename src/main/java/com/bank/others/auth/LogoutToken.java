package com.bank.others.auth;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "logout_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoutToken {
    @Id
    @Column(nullable = false, unique = true, length = 1000)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @OneToOne
    @JoinColumn(name = "user_session_id", foreignKey = @ForeignKey(name = "fk_logout_token_user_session_id"))
    private UserSession userSession;
}

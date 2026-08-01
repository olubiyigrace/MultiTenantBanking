package com.bank.entities;

import com.bank.enums.OtpPurpose;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Otp extends AbstractEntity{
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OtpPurpose purpose;

    @Builder.Default
    private Boolean used = false;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime expiresAt;
}


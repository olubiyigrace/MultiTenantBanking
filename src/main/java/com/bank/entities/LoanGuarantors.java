package com.bank.entities;


import com.bank.common.AbstractEntity;
import com.bank.enums.GuarantorStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "loan_guarantors")
public class LoanGuarantors extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    private LoanApplication loanApplication;

    private UUID guarantorMemberId;

    @Enumerated(EnumType.STRING)
    private GuarantorStatus guarantorStatus;

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][.SS][.S]")
    private LocalDateTime respondedAt;
}

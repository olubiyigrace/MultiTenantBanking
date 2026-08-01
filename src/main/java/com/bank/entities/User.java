package com.bank.entities;

import com.bank.enums.UserAccountType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Table(name = "users")
public class User extends AbstractEntity implements UserDetails {
    @Column(nullable = false, updatable = false)
    private String name;

    @Column(nullable = false, updatable = false)
    private String username;

    @Column(nullable = false)
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

    @Builder.Default
    private Boolean isVerified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", foreignKey = @ForeignKey(name = "fk_user_institution_id"))
    private Institution institution;

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<MemberProfile> memberProfiles = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userAccountType.name()));
    }

    public String getInstitutionId(){
        if(this.institution != null){
            return this.institution.getId();
        }
        return null;
    }

}
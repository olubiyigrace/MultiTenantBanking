package com.bank.services;

import com.bank.entities.User;
import com.bank.repositories.InstitutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

//@Service
//@RequiredArgsConstructor
//public class CustomDetailsService implements UserDetailsService {
//    private final InstitutionRepository institutionRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user = institutionRepository.findByEmail(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
//        return new org.springframework.security.core.userdetails.User(
//                user.getUsername(),
//                user.getPassword(),
//                getAuthority(user)
//        );
//    }
//    private Collection<? extends GrantedAuthority> getAuthority(User user) {
//        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getRole().name());
//        return List.of(grantedAuthority);
//    }
//}

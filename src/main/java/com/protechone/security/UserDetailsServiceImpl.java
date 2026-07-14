package com.protechone.security;

import com.protechone.entity.User;
import com.protechone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads application users for Spring Security, authenticating by email or
 * username and mapping the assigned role to a single Spring authority
 * (ROLE_<CODE>) which controllers guard with @PreAuthorize.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmailIgnoreCase(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("No user found for: " + usernameOrEmail));

        String roleCode = user.getRole() != null ? user.getRole().getCode() : "EMPLOYEE";

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + roleCode)))
                .accountLocked(Boolean.TRUE.equals(user.getIsLocked()))
                .disabled(!Boolean.TRUE.equals(user.getIsActive()))
                .build();
    }
}

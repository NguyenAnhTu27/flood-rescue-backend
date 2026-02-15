package com.floodrescue.config.security;

import com.floodrescue.module.user.entity.UserEntity;
import com.floodrescue.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) {
        // Ở đây username chính là userId (subject trong JWT)
        Long userId = Long.parseLong(username);

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String role = user.getRole().getCode(); // "CITIZEN"...
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        return new org.springframework.security.core.userdetails.User(
                String.valueOf(user.getId()),
                user.getPasswordHash(),
                user.getStatus() == 1,
                true, true, true,
                authorities
        );
    }
}
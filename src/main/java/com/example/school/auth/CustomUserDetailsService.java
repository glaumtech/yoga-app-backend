package com.example.school.auth;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AuthRep regRepository;

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User reg = regRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"+email));

        return new CustomUserDetails(
                reg.getEmail(),
                reg.getPassword(),
                true, true, true, true,
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")),
                reg.getId()


        );

    }
}


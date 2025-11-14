package com.example.school.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;
@Getter
@Setter
public class CustomUserDetails extends org.springframework.security.core.userdetails.User {

    private long id;
    private String username;
    private String password;
    private String email;
    private int organizationId;


    public CustomUserDetails(String username, String password, boolean enabled, boolean accountNonExpired,
                             boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities,
                             long userId) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.id = userId;
        this.username = username;
        this.password = password;

    }
}


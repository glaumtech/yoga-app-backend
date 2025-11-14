package com.example.school.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired AuthRep authRep;
    public Optional<Register> findByName(String name) {
        return authRep.findByUsername(name);
    }
    public void saves(Register reg) {
        authRep.save(reg);
    }
}

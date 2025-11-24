package com.example.school.auth;

import com.example.school.role.Role;
import com.example.school.role.RoleRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired AuthRep authRep;

    @Autowired
    private RoleRep roleRep;
    public Optional<User> findByEmailOrPhoneNo(String email, Long phoneNo) {
        return authRep.findByEmailOrPhoneNo(email,phoneNo);
    }
    public Optional<User> findByEmail(String email) {
        return authRep.findByEmail(email);
    }


    public User saveUser(UserRequest request) {

            User newUser = new User();

            newUser.setPhoneNo(request.getPhoneNo());

            // Set role
            if ("user".equalsIgnoreCase(request.getRole())) {
                Role userRole = roleRep.findByName("USER")
                        .orElseThrow(() -> new RuntimeException("Role not found"));
                newUser.setRole(userRole);
            }

            newUser.setEmail(request.getEmail());
            newUser.setUsername(request.getUsername());


            // Encode password
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setConfirmPassword(request.getPassword());

            return authRep.save(newUser);
        }


}

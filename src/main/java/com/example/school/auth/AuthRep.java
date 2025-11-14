package com.example.school.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRep extends JpaRepository<Register,Long> {
    Optional<Register> findByUsername(String name);
}

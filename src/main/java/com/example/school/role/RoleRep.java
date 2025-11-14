package com.example.school.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRep extends JpaRepository<Role,Long> {
    Optional<Role> findByName(String name);
}

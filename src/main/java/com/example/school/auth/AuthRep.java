package com.example.school.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRep extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String name);
    @Query("SELECT r FROM User r " +
            "WHERE r.email = :email " +
            "OR (:phoneNo IS NOT NULL AND r.phoneNo = :phoneNo)")
    Optional<User> findByEmailOrPhoneNo(@Param("email") String email, @Param("phoneNo") Long phoneNo);



}

package com.example.school.auth;

import com.example.school.role.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    private String username;
    private String email;
    private String password;
    private String confirmPassword;

    private Long phoneNo;
    private Long juryId; // link to Jury

    @ManyToOne
    private Role role;
}

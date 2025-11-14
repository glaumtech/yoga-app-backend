package com.example.school.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    private Long id;
    private String role;
    private String confirmPassword;
    private String password;
    private String email;
    private String newPassword;
    private String username;
    private Long phoneNo;
}

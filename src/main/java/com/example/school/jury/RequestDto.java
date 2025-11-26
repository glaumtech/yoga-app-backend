package com.example.school.jury;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestDto {
    private Long id;
    private boolean deleted =false;
    private String name;
    private String address;
    private String designation;
    private String password;
    private String confirmPassword;
    private String username;
    private String role;
    private String email;
}

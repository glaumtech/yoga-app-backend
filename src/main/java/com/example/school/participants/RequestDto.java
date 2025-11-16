package com.example.school.participants;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RequestDto {
    private Long id;

    private String participantName;

    private LocalDate dateOfBirth;
    private Long age;
    private String gender;
    private String category;
    private String schoolName;
    private String standard;
    private String yogaMasterName;
    private Long yogaMasterContact;
    private String address;
}

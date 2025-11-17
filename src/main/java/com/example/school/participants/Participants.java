package com.example.school.participants;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Participants {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String participantName;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateOfBirth;
    private Long age;
    private String gender;
    private String category;
    private String schoolName;
    private String standard;
    private String yogaMasterName;
    private String status;
    private Long yogaMasterContact;
    private String address;

    private boolean deleted = false;

    private String photo; // to store image filename

}

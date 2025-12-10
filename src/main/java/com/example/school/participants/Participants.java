package com.example.school.participants;

import javax.persistence.*;

import com.example.school.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Participants extends BaseEntity {
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
    private String participantCode;
    private boolean deleted = false;
//    private String groupName;
    private Long eventId;
    private String photo; // to store image filename

}

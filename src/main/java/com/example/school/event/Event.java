package com.example.school.event;

import javax.persistence.*;

import com.example.school.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Event extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    private String title;
    private String description;
    private String venue;
    private String venueAddress;
    private LocalDate startDate;
    private LocalDate endDate;
    private String fileName;
    private boolean active = false;
    private boolean current = false;
    private boolean deleted=false;
}

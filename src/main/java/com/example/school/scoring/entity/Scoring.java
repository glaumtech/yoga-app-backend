package com.example.school.scoring.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scoring")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Scoring {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id")
    private Long eventId;



    @Column(name = "grand_total")
    private Integer grandTotal;
    private Long juryId;
    private Long participantId;

    @Column(name = "deleted")
    private Boolean deleted = false;
}


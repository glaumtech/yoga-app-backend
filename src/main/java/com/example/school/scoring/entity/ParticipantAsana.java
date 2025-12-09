package com.example.school.scoring.entity;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ParticipantAsana {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scoring_id")
    private Long scoringId; // just store the ID

    @Column(name = "asana_name")
    private String asanaName;

    @Column(name = "score")
    private String score;

    @Column(name = "deleted")
    private Boolean deleted = false;

}

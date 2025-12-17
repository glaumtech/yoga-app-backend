package com.example.school.team;

import com.example.school.BaseEntity;
import com.example.school.jury.Jury;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Where;

import java.util.List;

@Entity
@Getter
@Setter

public class Team  extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;


    private Long eventId;

    @ManyToMany
    @Where(clause = "deleted = false")
    @JsonManagedReference
    @JoinTable(
            name = "team_jury",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "jury_id")
    )
    private List<Jury> juryList;
    private boolean deleted=false;

    // getters and setters
}


package com.example.school.jury;

import com.example.school.BaseEntity;
import com.example.school.role.Role;
import com.example.school.team.Team;
import com.fasterxml.jackson.annotation.JsonBackReference;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class  Jury  extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    private boolean deleted =false;
    private String name;
    private String address;
    private String designation;
    @ManyToMany(mappedBy = "juryList")
    @JsonBackReference
    private List<Team> teams;
    private Long userId;

}

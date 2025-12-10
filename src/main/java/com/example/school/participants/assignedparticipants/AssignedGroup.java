package com.example.school.participants.assignedparticipants;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.example.school.BaseEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AssignedGroup  extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;

    private Long teamId;




}

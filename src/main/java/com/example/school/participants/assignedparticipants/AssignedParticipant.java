package com.example.school.participants.assignedparticipants;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AssignedParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



private Long assignedGroupId;

    private Long participantId;

    private Long juryId;
    private boolean isScored=false;

    private String category; // Can be common/special per participant if needed

    private String status; // Assigned / Not Assigned
}

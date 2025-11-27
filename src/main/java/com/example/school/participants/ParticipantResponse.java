package com.example.school.participants;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantResponse {
    private Long id;
    private String participantName;
    private String status;
    private String assignmentStatus; // Assigned / Not Assigned
}

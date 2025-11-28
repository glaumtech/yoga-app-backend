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

    public ParticipantResponse(Long id, String participantName) {
        this.id=id;
        this.participantName=participantName;

    }
    public ParticipantResponse(){

    }


}

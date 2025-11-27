package com.example.school.participants.assignedparticipants;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantRequest {
    private String name;
    private Long id;
    public ParticipantRequest(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}

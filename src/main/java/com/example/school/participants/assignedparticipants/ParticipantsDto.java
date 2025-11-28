package com.example.school.participants.assignedparticipants;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantsDto {
    private String name;
    private Long id;
    private String group;
    private String schoolName;

    public ParticipantsDto(Long id, String name,String group,String schoolName) {
        this.id = id;
        this.name = name;
        this.group=group;
        this.schoolName=schoolName;
    }
}

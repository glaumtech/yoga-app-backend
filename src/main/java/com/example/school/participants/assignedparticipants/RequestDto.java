package com.example.school.participants.assignedparticipants;

import com.example.school.jury.JuryDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class RequestDto {
    private Long eventId;
    private List<JuryDto> juryDtos;
    private List<ParticipantsDto> participants;
    private String category;
    private Long teamId;
}

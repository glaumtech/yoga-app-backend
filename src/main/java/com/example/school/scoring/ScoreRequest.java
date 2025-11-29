package com.example.school.scoring;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ScoreRequest {
    private Long eventId;
    private List<ParticipantScoreRequest> scoreOfParticipants;
}

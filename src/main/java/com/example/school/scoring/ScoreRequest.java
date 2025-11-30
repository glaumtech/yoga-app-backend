package com.example.school.scoring;

import com.example.school.scoring.request.ParticipantScoreRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ScoreRequest {
    private Long eventId;

    private List<ParticipantScoreRequest> scoreOfParticipants;
}

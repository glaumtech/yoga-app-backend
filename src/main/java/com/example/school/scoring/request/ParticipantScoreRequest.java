package com.example.school.scoring.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ParticipantScoreRequest {
    private Long participantId;
    private Long assignId;
    private String category;
    private Integer grandTotal;   // Added
    private Long juryId; // optional
    private List<AsanaScoreRequest> asanas;
}

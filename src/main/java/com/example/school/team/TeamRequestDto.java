package com.example.school.team;

import com.example.school.jury.JuryDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TeamRequestDto {
    private Long id;
    private String name;
    private String category;
    private Long eventId;
    private List<JuryDto> juryList;
}

package com.example.school.team;
import java.util.stream.Collectors;
import com.example.school.jury.JuryDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class TeamGetDto {
    private Long id;
    private String name;
    private String category;
    private Long eventId;

    private List<JuryDto> juryList;

    public TeamGetDto(Team team) {
        this.id = team.getId();
        this.name = team.getName();
        this.category = team.getCategory();
        this.eventId = team.getEventId();
        this.juryList = team.getJuryList()
                .stream()
                .map(JuryDto::new)
                .collect(Collectors.toList());
    }
}

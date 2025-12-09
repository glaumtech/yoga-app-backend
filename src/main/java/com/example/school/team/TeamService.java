package com.example.school.team;

import com.example.school.event.Event;
import com.example.school.event.EventRep;
import com.example.school.jury.Jury;
import com.example.school.jury.JuryDto;
import com.example.school.jury.JuryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeamService {


        @Autowired
        private TeamRepository teamRepo;

        @Autowired
        private JuryRepository juryRepo;

        @Autowired
        private EventRep eventRepo;

        public Team createTeam(TeamRequestDto request) {
            if (teamRepo.existsByNameIgnoreCaseAndEventIdAndDeletedFalse(request.getName(), request.getEventId())) {
                throw new RuntimeException("Team with name '" + request.getName() + "' already exists!");
            }
            Team team = new Team();
            team.setName(request.getName());
            team.setCategory(request.getCategory());

            // Set event
            Event event = eventRepo.findById(request.getEventId())
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            team.setEventId(event.getId());

            // Fetch juries from DB
            List<Long> juryIds = request.getJuryList().stream()
                    .map(JuryDto::getId)
                    .collect(Collectors.toList());
            for (Long juryId : juryIds) {
                List<Team> assignedTeams =
                        teamRepo.findTeamsByJuryAndEvent(juryId, request.getEventId());

                if (!assignedTeams.isEmpty()) {
                    throw new RuntimeException("Jury ID " + juryId + " is already assigned to another team!");
                }
            }
            // Fetch juries from DB
            List<Jury> juries = juryRepo.findAllById(juryIds);
            team.setJuryList(juries);

            return teamRepo.save(team); // JPA handles mapping table automatically
        }

    public List<TeamGetDto> getAllTeams(Long eventId) {
        List<Team> teams = teamRepo.findAllByDeletedFalseAndEventId(eventId);
        return teams.stream()
                .map(TeamGetDto::new)
                .collect(Collectors.toList());
    }

    public Team updateTeam(Long id, TeamRequestDto request) {
        if (teamRepo.existsByNameIgnoreCaseAndIdNotAndDeletedFalse(request.getName(), request.getId())) {
            throw new RuntimeException("Another team with this name already exists!");
        }
        Team team = teamRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        team.setName(request.getName());
        team.setCategory(request.getCategory());

        // Set event
        Event event = eventRepo.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));
        team.setEventId(event.getId());
        List<Long> juryIds = request.getJuryList().stream()
                .map(JuryDto::getId)
                .collect(Collectors.toList());

        // Fetch juries from DB
        List<Jury> juries = juryRepo.findAllById(juryIds);
        team.setJuryList(juries);
        return teamRepo.save(team); // JPA handles mapping table automatically
    }
    public void deleteData(Long id) {
        Optional<Team> optionalTeam = teamRepo.findById(id);


        if (optionalTeam.isPresent()) {
            Team team = optionalTeam.get();



            team.setDeleted(true); // Soft delete
            teamRepo.save(team);
        } else {
            throw new RuntimeException("Team not found with ID: " + id);
        }
    }

}



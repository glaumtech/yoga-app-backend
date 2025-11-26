package com.example.school.team;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/team")
public class TeamController {

    @Autowired
    private TeamService teamService;

    // Create Team
    @PostMapping("/register")
    public ResponseEntity<?> createTeam(@RequestBody TeamRequestDto request) {

        Map<String, Object> response = new HashMap<>();

        try {
            Team savedTeam = teamService.createTeam(request);

            Map<String, Object> data = new HashMap<>();
            data.put("team", savedTeam);

            response.put("status", "success");
            response.put("message", "Team saved successfully!");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Get all teams (optional)
    @GetMapping("/all")
    public ResponseEntity<?> getAllTeams() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TeamGetDto> teams = teamService.getAllTeams();
            Map<String, Object> data = new HashMap<>();
            data.put("teams", teams);

            response.put("status", "success");
            response.put("message", "Teams fetched successfully!");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Update Team
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTeam(
            @PathVariable Long id,
            @RequestBody TeamRequestDto request) {

        Map<String, Object> response = new HashMap<>();

        try {
            Team updatedTeam = teamService.updateTeam(id, request);

            Map<String, Object> data = new HashMap<>();
            data.put("team", updatedTeam);

            response.put("status", "success");
            response.put("message", "Team updated successfully!");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTeam(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            teamService.deleteData(id);

            response.put("status", "success");
            response.put("message", "Team deleted successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}


package com.example.school.scoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/scoring")
public class ScoringController {

    @Autowired
    private ScoringService scoringService;




        @PostMapping("/save")
        public ResponseEntity<Map<String, Object>> saveScores(@RequestBody ScoreRequest request) {
            Map<String, Object> response = new HashMap<>();
            try {
                List<Map<String, Object>> savedParticipants = scoringService.saveScores(request);

                response.put("status", "success");
                response.put("message", "Scores saved successfully!");
                response.put("data", Map.of("participants", savedParticipants));

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                response.put("status", "error");
                response.put("message", e.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        }



        // GET scores for an event
        @GetMapping("/event/{eventId}")
        public ResponseEntity<Map<String, Object>> getScores(@PathVariable Long eventId) {
            Map<String, Object> response = new HashMap<>();
            try {
                // Retrieve scores from service
                List<Map<String, Object>> participantsScores = scoringService.getScoresByEvent(eventId);

                response.put("status", "success");
                response.put("message", "Scores retrieved successfully!");
                response.put("data", Map.of("scoreOfParticipants", participantsScores));

                return ResponseEntity.ok(response);

            } catch (Exception e) {
                response.put("status", "error");
                response.put("message", e.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        }


}



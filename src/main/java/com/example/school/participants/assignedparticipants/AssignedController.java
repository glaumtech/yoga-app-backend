package com.example.school.participants.assignedparticipants;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assign-participants")
public class AssignedController {
    @Autowired
    private AssignedService assignedService;
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> assignParticipants(@RequestBody RequestDto req) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Call service
            List<AssignedParticipant> savedAssignments = assignedService.assignParticipants(req);

            // Prepare data block
            Map<String, Object> data = new HashMap<>();
            data.put("assignments", savedAssignments);

            // Success response
            response.put("status", "success");
            response.put("message", "Participants assigned successfully!");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            // Error response
            response.put("status", "error");
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

//    @GetMapping("/{eventId}")
//    public ResponseEntity<List<AssignedParticipant>> getByEvent(@PathVariable Long eventId) {
//        return ResponseEntity.ok(service.getAssignmentsByEvent(eventId));
//    }

    @GetMapping("/{eventId}/jury/{juryId}")
    public ResponseEntity<Map<String, Object>> getParticipantsByJury(
            @PathVariable Long eventId,
            @PathVariable Long juryId
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ParticipantRequest> participants = assignedService.getParticipantsForJury(eventId, juryId);

            Map<String, Object> data = new HashMap<>();
            data.put("participants", participants);

            response.put("status", "success");
            response.put("message", "Assigned Participants fetched successfully");
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}

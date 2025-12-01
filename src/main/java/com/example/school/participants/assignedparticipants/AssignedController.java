package com.example.school.participants.assignedparticipants;


import com.example.school.participants.PageFilterRequest;
import com.example.school.participants.ParticipantResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/assign-participants")
public class AssignedController {
    @Autowired
    private AssignedService assignmentService;




        /**
         * Assign participants to juries under a team/event
         */
        @PostMapping("/assign")
        public ResponseEntity<Map<String, Object>> assignParticipants(@RequestBody RequestDto request) {
            Map<String, Object> response = new HashMap<>();
            try {
                List<AssignedParticipant> assigned = assignmentService.assignParticipants(request);

                response.put("status", "success");
                response.put("message", "Participants assigned successfully!");
                Map<String, Object> user = new HashMap<>();
                user.put("assigned",assigned);
                response.put("data", user);

                return ResponseEntity.ok(response);
            } catch (Exception e) {
                e.printStackTrace();
                response.put("status", "error");
                response.put("message", e.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        }


    @PostMapping("/{eventId}/assignments")
    public ResponseEntity<Map<String, Object>> getAssignments(
            @PathVariable Long eventId,
            @RequestBody PageFilterRequest request
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = assignmentService.getParticipantsAndJuriesByEvent(eventId, request);

            response.put("status", "success");
            response.put("message", "Participants retrieved successfully!");


            response.put("data", data); // data from service


            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message",  e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    @GetMapping("/event/{eventId}")
    public ResponseEntity<Map<String, Object>> getParticipantsByJury(
            @PathVariable Long eventId,
            @RequestParam(required = false) Long juryId  // optional
    ) {
        Map<String, Object> response = new HashMap<>();


        try {
            Map<String, Object> data = assignmentService.getParticipantsForJury(eventId, juryId);
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

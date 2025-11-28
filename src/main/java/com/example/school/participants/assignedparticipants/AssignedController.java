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
                response.put("data", assigned);

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
            response.put("message", "Failed to retrieve participants: " + e.getMessage());
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


//    @PostMapping("/save")
//    public ResponseEntity<Map<String, Object>> assignParticipants(@RequestBody RequestDto req) {
//
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            // Call service
//            List<AssignedParticipant> savedAssignments = assignedService.assignParticipants(req);
//
//            // Prepare data block
//            Map<String, Object> data = new HashMap<>();
//            data.put("assignments", savedAssignments);
//
//            // Success response
//            response.put("status", "success");
//            response.put("message", "Participants assigned successfully!");
//            response.put("data", data);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//
//            // Error response
//            response.put("status", "error");
//            response.put("message", e.getMessage());
//
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//
//
//
//    @GetMapping("/event/{eventId}")
//    public ResponseEntity<Map<String, Object>> getParticipantsByJury(
//            @PathVariable Long eventId,
//            @RequestParam(required = false) Long juryId  // optional
//    ) {
//        Map<String, Object> response = new HashMap<>();
//
//        try {
//            List<ParticipantRequest> participants = assignedService.getParticipantsForJury(eventId, juryId);
//
//            Map<String, Object> data = new HashMap<>();
//            data.put("participants", participants);
//
//            response.put("status", "success");
//            response.put("message", "Assigned Participants fetched successfully");
//            response.put("data", data);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            response.put("status", "error");
//            response.put("message", e.getMessage());
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
//    @PostMapping("/{eventId}/participants")
//    public ResponseEntity<Map<String, Object>> getParticipantsByEvent(
//            @PathVariable Long eventId,
//            @RequestBody PageFilterRequest request
//    ) {
//        Page<ParticipantRequest> participantsPage = assignedService.getParticipantsByEvent(eventId, request);
//
//        Map<String, Object> response = new HashMap<>();
//        Map<String, Object> dataMap = new HashMap<>();
//        dataMap.put("users", participantsPage.getContent());
//        dataMap.put("currentPage", participantsPage.getNumber());
//        dataMap.put("totalItems", participantsPage.getTotalElements());
//        dataMap.put("totalPages", participantsPage.getTotalPages());
//        response.put("status", "success");
//        response.put("message", "Assigned Participants retrieved successfully!");
//        response.put("data", dataMap);
//
//
//        return ResponseEntity.ok(response);
//    }

}

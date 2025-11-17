package com.example.school.participants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/participants")
public class ParticipantController {


    @Autowired
    private ParticipantService participantService;
//    @PostMapping("/register")
//    public ResponseEntity<Map<String, String>> submitData(@RequestPart("data") String data,
//
//                                                          @RequestPart(value = "photo", required = false) MultipartFile photo) {
//
//        try {
//          //  participantService.save(data, photo);
//            Participants newUser = participantService.save(data, photo);
//            Map<String, Object> datas = new HashMap<>();
//            Map<String, Object> userMap = new HashMap<>();
//            userMap.put("id", newUser.getId());
//            userMap.put("participantName",newUser.getParticipantName());
//            userMap.put("dateOfBirth",newUser.getDateOfBirth());
//            userMap.put("age",newUser.getAge());
//            userMap.put("gender",newUser.getGender());
//            userMap.put("category",newUser.getCategory());
//            userMap.put("schoolName",newUser.getSchoolName());
//            userMap.put("standard",newUser.getStandard());
//            userMap.put("yogaMasterName",newUser.getYogaMasterName());
//            userMap.put("yogaMasterContact", newUser.getYogaMasterContact());
//            userMap.put("address",newUser.getAddress());
//            user.put("id",newUser.getId());
//            data.put("user",user);
//
//            // Build response
//            response.put("status", "success");
//            response.put("message", "Data saved successfully!");
//            response.put("data", data);
//
//            return ResponseEntity.ok(response);
//            //return ResponseEntity.ok(Collections.singletonMap("message", "Data saved successfully!"));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "Error saving data!"));
//        }
//
//    }
@PostMapping("/register")
public ResponseEntity<Map<String, Object>> submitData(
        @RequestPart("data") String data, // JSON string
        @RequestPart(value = "photo", required = false) MultipartFile photo) {

    Map<String, Object> response = new HashMap<>();
    try {
        // Convert JSON string to DTO
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        RequestDto requestDto = mapper.readValue(data, RequestDto.class);

        // Save participant
        Participants newUser = participantService.save(requestDto, photo);

        // Build user map
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", newUser.getId());
        userMap.put("participantName", newUser.getParticipantName());
        userMap.put("dateOfBirth", newUser.getDateOfBirth());
        userMap.put("age", newUser.getAge());
        userMap.put("gender", newUser.getGender());
        userMap.put("category", newUser.getCategory());
        userMap.put("schoolName", newUser.getSchoolName());
        userMap.put("status",newUser.getStatus());
        userMap.put("standard", newUser.getStandard());
        userMap.put("yogaMasterName", newUser.getYogaMasterName());
        userMap.put("yogaMasterContact", newUser.getYogaMasterContact());
        userMap.put("address", newUser.getAddress());


        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("user", userMap);

        // Final response
        response.put("status", "success");
        response.put("message", "Data saved successfully!");
        response.put("data", dataMap);

        return ResponseEntity.ok(response);

    } catch (Exception e) {
        e.printStackTrace();
        response.put("status", "error");
        response.put("message", "Error saving data!");
        return ResponseEntity.badRequest().body(response);
    }
}
    // URL example: /status_verify/1/accepted
    @GetMapping("/status_verify/{id}/{status}")
    public ResponseEntity<Map<String,Object>> updateStatus(
            @PathVariable Long id,
            @PathVariable String status) {

        Map<String, Object> response = new HashMap<>();
        try {
            Participants participant = participantService.updateStatus(id, status);

            Map<String, Object> dataMap = new HashMap<>();
            Map<String,Object> user=new HashMap<>();
            user.put("id", participant.getId());
            user.put("participantName", participant.getParticipantName());
            user.put("status", participant.getStatus());
            dataMap.put("user",user);

            response.put("status", "success");
            response.put("message", "Participant status updated successfully!");
            response.put("data", dataMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getAllParticipants() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Participants> participantsList = participantService.getAllParticipants();

            List<Map<String, Object>> users = new ArrayList<>();
            for (Participants p : participantsList) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", p.getId());
                userMap.put("participantName", p.getParticipantName());
                userMap.put("dateOfBirth", p.getDateOfBirth());
                userMap.put("age", p.getAge());
                userMap.put("gender", p.getGender());
                userMap.put("category", p.getCategory());
                userMap.put("schoolName", p.getSchoolName());
                userMap.put("standard", p.getStandard());
                userMap.put("yogaMasterName", p.getYogaMasterName());
                userMap.put("yogaMasterContact", p.getYogaMasterContact());
                userMap.put("address", p.getAddress());
                userMap.put("photo", p.getPhoto());
                userMap.put("status",p.getStatus());

                users.add(userMap);
            }

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("users", users); // list of all participants

            response.put("status", "success");
            response.put("message", "Participants retrieved successfully!");
            response.put("data", dataMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error retrieving participants!");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>>  getFilteredTax(@RequestBody PageFilterRequest filter) {
        Map<String, Object> response = new HashMap<>();

        try {
            Page<Participants> resultPage = participantService.getFiltered(filter);
            Page<ParticipantsDto> dtoPage = resultPage.map(ParticipantsDto::new);
            // return ResponseEntity.ok(dtoPage);
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("users", dtoPage.getContent());
            dataMap.put("currentPage", dtoPage.getNumber());
            dataMap.put("totalItems", dtoPage.getTotalElements());
            dataMap.put("totalPages", dtoPage.getTotalPages());

            response.put("status", "success");
            response.put("message", "Participants retrieved successfully!");
            response.put("data", dataMap);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error retrieving participants!");
            return ResponseEntity.badRequest().body(response);
        }
    }
}

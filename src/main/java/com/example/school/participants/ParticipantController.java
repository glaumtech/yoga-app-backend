package com.example.school.participants;

import com.example.school.event.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/participants")
public class ParticipantController {


    @Autowired
    private ParticipantService participantService;
    @Autowired
    private ParticipantRep participantRep;

@PostMapping("/{eventId}/register")
public ResponseEntity<Map<String, Object>> submitData(
        @RequestPart("data") String data, // JSON string
        @RequestPart(value = "file", required = false) MultipartFile photo,@PathVariable Long eventId) {

    Map<String, Object> response = new HashMap<>();
    try {
        // Convert JSON string to DTO
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        RequestDto requestDto = mapper.readValue(data, RequestDto.class);

        // Save participant
        Participants newUser = participantService.save(requestDto, photo,eventId);

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
        userMap.put("participantCode",newUser.getParticipantCode());
        userMap.put("eventId",newUser.getEventId());


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
    @GetMapping("/getById/{id}")
    public ResponseEntity<Map<String,Object>> getParticipantById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Participants p = participantService.getById(id);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", p.getId());
            userMap.put("participantName", p.getParticipantName());
            userMap.put("dateOfBirth", p.getDateOfBirth());
            userMap.put("age", p.getAge());
            userMap.put("gender", p.getGender());
            userMap.put("category", p.getCategory());
            userMap.put("schoolName", p.getSchoolName());
            userMap.put("status",p.getStatus());
            userMap.put("standard", p.getStandard());
            userMap.put("yogaMasterName", p.getYogaMasterName());
            userMap.put("yogaMasterContact", p.getYogaMasterContact());
            userMap.put("address", p.getAddress());
            userMap.put("participantCode",p.getParticipantCode());
            userMap.put("eventId",p.getEventId());


            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("user", userMap);

            // Final response
            response.put("status", "success");
            response.put("message", "Data retrieved successfully!");
            response.put("data", dataMap);

            return ResponseEntity.ok(response);
        }
        catch (Exception e){

                e.printStackTrace();
                response.put("status", "error");
                response.put("message", "Error in getting data!");
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



       @PostMapping("/{eventId}/eventbased")
    public ResponseEntity<Map<String, Object>>  getFilteredbyEvent(@RequestBody PageFilterRequest filter,@PathVariable Long eventId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Page<ParticipantsDto> dtoPage = participantService.getFilteredByEvent(eventId,filter);
           // Page<ParticipantsDto> dtoPage = resultPage.map(ParticipantsDto::new);
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

    @GetMapping("/image/{id}")
    public ResponseEntity<?> getImageById(@PathVariable Long id) throws IOException {

        Map<String, Object> response = new HashMap<>();

        // 1️⃣ Find the Event
        Optional<Participants> optionalParticipants = participantRep.findById(id);
        if (optionalParticipants.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Participant not found with id " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Participants participants = optionalParticipants.get();
        String filename = participants.getPhoto();

        if (filename == null || filename.isEmpty()) {
            response.put("status", "error");
            response.put("message", "No file found for this participant");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // 2️⃣ Get the file
        Path filePath = Paths.get(System.getProperty("user.dir") + "/uploads").resolve(filename);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            response.put("status", "error");
            response.put("message", "File not found on disk");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        // 3️⃣ Detect content type
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        // 4️⃣ Return the file as Resource
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String,Object>> update(
            @PathVariable Long id,
            @RequestPart("data") String data,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Convert JSON string to DTO
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            RequestDto requestDto = mapper.readValue(data,RequestDto.class);

            // Save participant
            Participants newUser = participantService.update(requestDto, file, id);

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
            dataMap.put("event", userMap);

            // Final response
            response.put("status", "success");
            response.put("message", "Data updated successfully!");
            response.put("data", dataMap);

            return ResponseEntity.ok(response);

            //return eventService.updateItems(itemJson, file, id);


        } catch (Exception e) {

            e.printStackTrace();
            response.put("status", "error");
            response.put("message", "Error saving data!");
            return ResponseEntity.badRequest().body(response);


        }

    }

    @GetMapping("/{id}/print")
    public ResponseEntity<byte[]> printParticipant(@PathVariable Long id) {
        byte[] pdf = participantService.generateParticipantPdf(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "inline; filename=participant_" + id + ".pdf")
                .body(pdf);
    }
    @GetMapping("/{id}/certificate")
    public ResponseEntity<byte[]> getCertificate(@PathVariable Long id) {
        try {
            byte[] pdfBytes = participantService.generateCertificatePdf(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("inline")
                    .filename("Certificate_" + id + ".pdf")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



}

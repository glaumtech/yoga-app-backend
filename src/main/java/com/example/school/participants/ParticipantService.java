package com.example.school.participants;

import com.example.school.event.Event;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class ParticipantService {

    @Autowired
    private ParticipantRep participantRep;

    //    public ResponseEntity<Map<String, String>> save(String data, MultipartFile photo)
//            throws IOException {
    public Participants save(RequestDto data, MultipartFile photo,Long eventId) throws IOException {
        ObjectMapper mapper = new ObjectMapper(); // it from jackson library in spirng boot to handle jso
        // Json to javaObject
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Participants participants = new Participants();

        // populate fields
        participants.setParticipantName(data.getParticipantName());
        participants.setDateOfBirth(data.getDateOfBirth());
        participants.setAge(data.getAge());
        participants.setEventId(eventId);

        participants.setGender(data.getGender());
        participants.setCategory(data.getCategory());
        participants.setSchoolName(data.getSchoolName());
        participants.setStandard(data.getStandard());
        participants.setYogaMasterName(data.getYogaMasterName());
        participants.setYogaMasterContact(data.getYogaMasterContact());
        participants.setAddress(data.getAddress());
        participants.setStatus("Requested");


        Optional<Participants> lastProduct = participantRep.findTopByOrderByIdDesc();

        String newId = "MEM0001"; // Default if no previous ID exists

        if (lastProduct.isPresent()) {
            String lastId = lastProduct.get().getParticipantCode();

            if (lastId != null && !lastId.isEmpty()) {
                // Extract number part after prefix "MEM"
                int lastNumber = Integer.parseInt(lastId.replace("MEM", ""));
                newId = String.format("MEM%04d", lastNumber + 1); // Increment and format
            }
        }

        participants.setParticipantCode(newId);

        // ✅ Handle file saving (optional)
        if (photo != null && !photo.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            File directory = new File(uploadDir);
            if (!directory.exists())
                directory.mkdirs();

            String fileName = photo.getOriginalFilename();

            String filePath = uploadDir + "/" + fileName; // fixed path handling
            photo.transferTo(new File(filePath));
            participants.setPhoto(fileName);
        } else {
            participants.setPhoto(null);
        }

        return participantRep.save(participants);


    }

    @Transactional
    public Participants updateStatus(Long id, String status) {
        Participants participant = participantRep.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        participant.setStatus(status);
        return participantRep.save(participant); // save and return updated entity
    }

//    public List<Participants> getAllParticipants() {
//        return participantRep.findByStatus("Accepted"); // only accepted
//    }

    public Page<Participants> getFilteredbyEvent(PageFilterRequest filter,Long eventId) {
        Pageable pageable = filter.toPageable();
        String name = filter.getParticipantName();
        String status = filter.getStatus();

        if ((name == null || name.isEmpty()) && (status == null || status.isEmpty())) {
            // No name or status filter → return all except Rejected
            return participantRep.findAllExcludingStatusByEvent(eventId ,pageable);
        }

        if ((name == null || name.isEmpty())) {
            // Only status filter applied
            return participantRep.findByStatusExcludingRejectedByEvent(status,eventId,pageable);
        }

        if (status == null || status.isEmpty()) {
            // Only name search applied
            return participantRep.findByNameExcludingStatusByEvent(name,eventId,pageable);
        }

        // Both name and status applied
        return participantRep.findByNameAndStatusExcludingRejectedByEvent(name, status,eventId,pageable);
    }
    public Page<Participants> getFiltered(PageFilterRequest filter) {
        Pageable pageable = filter.toPageable();
        String name = filter.getParticipantName();
        String status = filter.getStatus();

        if ((name == null || name.isEmpty()) && (status == null || status.isEmpty())) {
            // No name or status filter → return all except Rejected
            return participantRep.findAllExcludingStatus(pageable );
        }

        if ((name == null || name.isEmpty())) {
            // Only status filter applied
            return participantRep.findByStatusExcludingRejected(status, pageable);
        }

        if (status == null || status.isEmpty()) {
            // Only name search applied
            return participantRep.findByNameExcludingStatus(name, pageable);
        }

        // Both name and status applied
        return participantRep.findByNameAndStatusExcludingRejected(name, status, pageable);
    }

    @Transactional
    public Participants update(RequestDto data, MultipartFile file, Long id) throws IOException {

        Participants participants = participantRep.findById(id)
                .orElseThrow(() -> new RuntimeException("Participant not found!"));

        participants.setParticipantName(data.getParticipantName());
        participants.setDateOfBirth(data.getDateOfBirth());
        participants.setAge(data.getAge());
        //participants.setEventId(eventId);

        participants.setGender(data.getGender());
        participants.setCategory(data.getCategory());
        participants.setSchoolName(data.getSchoolName());
        participants.setStandard(data.getStandard());
        participants.setYogaMasterName(data.getYogaMasterName());
        participants.setYogaMasterContact(data.getYogaMasterContact());
        participants.setAddress(data.getAddress());
        //participants.setStatus("Requested");
        // File upload → EXACT same style as Event save()
        if (file != null && !file.isEmpty()) {

            String uploadDir = System.getProperty("user.dir") + "/uploads";
            File directory = new File(uploadDir);

            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = file.getOriginalFilename();
            String filePath = uploadDir + "/" + fileName;

            file.transferTo(new File(filePath));

            participants.setPhoto(fileName);
        } else {
            participants.setPhoto(null);
        }

        return participantRep.save(participants);
    }

}

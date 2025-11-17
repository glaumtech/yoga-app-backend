package com.example.school.participants;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParticipantService {

    @Autowired
    private ParticipantRep participantRep;

    //    public ResponseEntity<Map<String, String>> save(String data, MultipartFile photo)
//            throws IOException {
    public Participants save(RequestDto data, MultipartFile photo) throws IOException {
        ObjectMapper mapper = new ObjectMapper(); // it from jackson library in spirng boot to handle jso
        // Json to javaObject
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Participants participants = new Participants();

        // populate fields
        participants.setParticipantName(data.getParticipantName());
        participants.setDateOfBirth(data.getDateOfBirth());
        participants.setAge(data.getAge());
        participants.setGender(data.getGender());
        participants.setCategory(data.getCategory());
        participants.setSchoolName(data.getSchoolName());
        participants.setStandard(data.getStandard());
        participants.setYogaMasterName(data.getYogaMasterName());
        participants.setYogaMasterContact(data.getYogaMasterContact());
        participants.setAddress(data.getAddress());
        participants.setStatus("Requested");

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

    public List<Participants> getAllParticipants() {
        return participantRep.findByStatus("Accepted"); // only accepted
    }

    //    public Page<Participants> getFiltered(PageFilterRequest filter) {
//        Pageable pageable = filter.toPageable();
//
//        if (filter.getParticipantName() == null || filter.getParticipantName().isEmpty()) {
//
//
//            return participantRep.findAllExcludingStatus("Rejected", pageable);
//        } else {
//            return participantRep.findByNameExcludingStatus(filter.getParticipantName(),"Rejected", pageable);
//        }
//    }
//}
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
}

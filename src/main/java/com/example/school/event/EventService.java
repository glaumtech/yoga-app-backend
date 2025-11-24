package com.example.school.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRep eventRep;

    public Event save(RequestDto data, MultipartFile photo) throws IOException {
        ObjectMapper mapper = new ObjectMapper(); // it from jackson library in spirng boot to handle jso
        // Json to javaObject
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Event events = new Event();

        // populate fields
        events.setTitle(data.getTitle());
        events.setDescription(data.getDescription());
        events.setVenue(data.getVenue());
        events.setVenueAddress(data.getVenueAddress());
        events.setStartDate(data.getStartDate());
        events.setEndDate(data.getEndDate());
        events.setActive(data.isActive());
        events.setCurrent(data.isCurrent());


        // ✅ Handle file saving (optional)
        if (photo != null && !photo.isEmpty()) {
            String uploadDir = System.getProperty("user.dir") + "/uploads";
            File directory = new File(uploadDir);
            if (!directory.exists())
                directory.mkdirs();

            String fileName = photo.getOriginalFilename();

            String filePath = uploadDir + "/" + fileName; // fixed path handling
            photo.transferTo(new File(filePath));
            events.setFileName(fileName);
        } else {
            events.setFileName(null);
        }

        return eventRep.save(events);


    }


    @Transactional
    public Event updateEvent(RequestDto data, MultipartFile file, Long id) throws IOException {

        Event event = eventRep.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found!"));

        // populate fields
        event.setTitle(data.getTitle());
        event.setDescription(data.getDescription());
        event.setVenue(data.getVenue());
        event.setVenueAddress(data.getVenueAddress());
        event.setStartDate(data.getStartDate());
        event.setEndDate(data.getEndDate());
        event.setActive(data.isActive());
        event.setCurrent(data.isCurrent());

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

            event.setFileName(fileName);
        } else {
            event.setFileName(null);
        }

        return eventRep.save(event);
    }
    public List<Event> getAllEvents() {
        return eventRep.findByDeletedFalse(); // only accepted
   }

    public void deleteData(Long id) {
        Optional<Event> optionalEvent = eventRep.findById(id);


        if (optionalEvent.isPresent()) {
            Event event = optionalEvent.get();



            event.setDeleted(true); // Soft delete
            eventRep.save(event);
        } else {
            throw new RuntimeException("Event not found with ID: " + id);
        }
    }


}

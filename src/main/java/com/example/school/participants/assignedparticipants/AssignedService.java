package com.example.school.participants.assignedparticipants;

import com.example.school.jury.JuryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.ast.Assign;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AssignedService {
    @Autowired
    private AssignedRepo assignedRepo;



//
//        public void assignParticipants(RequestDto req) {
//System.out.println("category"+req.getCategory());
//            for (JuryDto jury : req.getJuryDtos()) {
//
//                for (ParticipantRequest participant : req.getParticipants()) {
//
//                     boolean alreadyAssigned = assignedRepo.existsByEventId(
//                            req.getEventId()
//
//                    );
//
//                    if (alreadyAssigned) {
//                        // skip or throw exception if you want
//                        System.out.println("Participant " + participant.getName() +
//                                " is already assigned in event " + req.getEventId() +
//                                " under category " + req.getCategory());
//                        continue; // skip this participant
//                    }
//
//                    Long eventId = req.getEventId();
//                    Long juryId = jury.getId();
//                    Long participantId = participant.getId();
//
//                    // Check if assignment already exists
////                    boolean exists = assignedRepo.existsByEventIdAndJuryIdAndParticipantId(
////                            eventId, juryId, participantId
////                    );
//
//                    if (!alreadyAssigned) {
//                        AssignedParticipant ap = new AssignedParticipant();
//                        ap.setEventId(eventId);
//                        ap.setJuryId(juryId);
//                        ap.setParticipantId(participantId);
//                        ap.setCategory(req.getCategory());
//
//                        assignedRepo.save(ap);
//                    }
//                }
//            }
//        }
public List<AssignedParticipant> assignParticipants(RequestDto req) {
    System.out.println("Category: " + req.getCategory());

    List<ParticipantRequest> participantsToAssign = new ArrayList<>();
    List<AssignedParticipant> savedAssignments = new ArrayList<>();

    // 1️⃣ First, filter out already assigned participants
    for (ParticipantRequest participant : req.getParticipants()) {
        boolean alreadyAssigned = assignedRepo.existsByEventIdAndCategoryAndParticipantId(
                req.getEventId(),
                req.getCategory(),
                participant.getId()
        );

        if (alreadyAssigned) {

                throw new RuntimeException(
                        "Participant " + participant.getName() +
                                " is already assigned in event " + req.getEventId() +
                                " under category " + req.getCategory()
                );


        } else {
            participantsToAssign.add(participant);
        }
    }

    // 2️⃣ Assign remaining participants to all juries
    for (JuryDto jury : req.getJuryDtos()) {
        for (ParticipantRequest participant : participantsToAssign) {
            AssignedParticipant ap = new AssignedParticipant();
            ap.setEventId(req.getEventId());
            ap.setJuryId(jury.getId());
            ap.setParticipantId(participant.getId());
            ap.setCategory(req.getCategory());

            AssignedParticipant saved = assignedRepo.save(ap);
            savedAssignments.add(saved);

            System.out.println("Saved: Participant " + participant.getName() +
                    " to Jury " + jury.getId());
        }
    }

    // Return all saved assignments
    return savedAssignments;
}

//        public List<AssignedParticipant> getAssignmentsByEvent(Long eventId) {
//            return repo.findAllByEventId(eventId);
//        }

//        public List<Long> getParticipantsForJury(Long eventId, Long juryId) {
//            return assignedRepo.findAllByEventIdAndJuryId(eventId, juryId)
//                    .stream()
//                    .map(AssignedParticipant::getParticipantId)
//                    .toList();
//        }
public List<ParticipantRequest> getParticipantsForJury(Long eventId, Long juryId) {
    return assignedRepo.findParticipantsByEventAndJury(eventId, juryId);
}


}

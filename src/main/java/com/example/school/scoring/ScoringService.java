package com.example.school.scoring;

import com.example.school.jury.JuryRepository;
import com.example.school.participants.ParticipantRep;
import com.example.school.scoring.entity.ParticipantAsana;
import com.example.school.scoring.entity.Scoring;
import com.example.school.scoring.request.AsanaScoreRequest;
import com.example.school.scoring.request.ParticipantScoreRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScoringService {
    @Autowired
    private final ScoringRepository scoringRepository;

    @Autowired
    private ParticipantRep participantRepository;

    @Autowired
    private JuryRepository juryRepository;


    @Autowired
    private ScoringRepository participantEventRepo; // Table 1

    @Autowired
    private ParticipantAsanaRepository participantAsanaRepo; // Table 2


    @Autowired
    private ParticipantRep participantRepo;

    @Transactional
    public List<Map<String, Object>> saveScores(ScoreRequest request) {

        List<Map<String, Object>> participantTotals = new ArrayList<>();

        for (ParticipantScoreRequest p : request.getScoreOfParticipants()) {

            // Validate participant
            if (!participantRepo.existsById(p.getParticipantId())) {
                throw new RuntimeException("Participant ID " + p.getParticipantId() + " does not exist");
            }

            // Save Participant Event (grand total)
            Scoring scoring = new Scoring();
            scoring.setEventId(request.getEventId());

            scoring.setGrandTotal(p.getGrandTotal());
            scoring.setParticipantId(p.getParticipantId());

            scoring.setJuryId(p.getJuryId());
            scoring = participantEventRepo.save(scoring);

            Map<String, Double> asanaTotalsMap = new HashMap<>();

            // Save Asanas
            for (AsanaScoreRequest a : p.getAsanas()) {
                ParticipantAsana participantAsana = new ParticipantAsana();
                participantAsana.setScoringId(scoring.getId());
                participantAsana.setAsanaName(a.getAsanaName());
                participantAsana.setScore(a.getScore());

                participantAsanaRepo.save(participantAsana);

                asanaTotalsMap.put(a.getAsanaName(), Double.parseDouble(a.getScore()));
            }

            Map<String, Object> pMap = new HashMap<>();
            pMap.put("participantId", p.getParticipantId());
            pMap.put("grandTotal", p.getGrandTotal());
            pMap.put("asanaTotals", asanaTotalsMap);

            participantTotals.add(pMap);
        }

        return participantTotals;
    }


    public List<Map<String, Object>> getScoresByEvent(Long eventId) {

        List<Scoring> scorings = participantEventRepo.findByEventIdAndDeletedFalse(eventId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Scoring pe : scorings) {
            Map<String, Object> pMap = new HashMap<>();
            pMap.put("participantId", pe.getParticipantId());
            pMap.put("grandTotal", pe.getGrandTotal());

            // Include juryId (assumes stored in participantEvent entity)
            pMap.put("juryId", pe.getJuryId()); // make sure ParticipantEvent has juryId column

            // Get asana scores
            List<ParticipantAsana> asanas = participantAsanaRepo.findByScoringIdAndDeletedFalse(pe.getId());
            List<Map<String, Object>> asanaList = new ArrayList<>();
            for (ParticipantAsana a : asanas) {
                Map<String, Object> asanaMap = new HashMap<>();
                asanaMap.put("asanaName", a.getAsanaName());
                asanaMap.put("score", a.getScore());
                asanaList.add(asanaMap);
            }

            pMap.put("asanas", asanaList);
            result.add(pMap);
        }

        return result;
    }


    public Map<String, Object> getScoresByEventAndParticipant(Long eventId, Long participantId) {

        // Fetch scoring row for this event + participant
        Scoring scoring =
                participantEventRepo.findByEventIdAndParticipantIdAndDeletedFalse(eventId, participantId);

        if (scoring == null) {
            throw new RuntimeException("No score found for participant " + participantId + " in event " + eventId);
        }

        Map<String, Object> participantMap = new HashMap<>();
        participantMap.put("participantId", scoring.getParticipantId());
        participantMap.put("grandTotal", scoring.getGrandTotal());
        participantMap.put("juryId", scoring.getJuryId());

        // Fetch all asana scores
        List<ParticipantAsana> asanas =
                participantAsanaRepo.findByScoringIdAndDeletedFalse(scoring.getId());

        List<Map<String, Object>> asanaList = new ArrayList<>();

        for (ParticipantAsana asana : asanas) {
            Map<String, Object> asanaMap = new HashMap<>();
            asanaMap.put("asanaName", asana.getAsanaName());
            asanaMap.put("score", asana.getScore());
            asanaList.add(asanaMap);
        }

        participantMap.put("asanas", asanaList);

        // Final response format
        Map<String, Object> response = new HashMap<>();
        response.put("eventId", eventId);
        response.put("scoreOfParticipant", participantMap);

        return response;
    }


}





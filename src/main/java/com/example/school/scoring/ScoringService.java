package com.example.school.scoring;

import com.example.school.jury.Jury;
import com.example.school.jury.JuryRepository;
import com.example.school.participants.ParticipantRep;
import com.example.school.participants.Participants;
import com.example.school.participants.assignedparticipants.AssignedGroup;
import com.example.school.participants.assignedparticipants.AssignedGroupRepository;
import com.example.school.participants.assignedparticipants.AssignedParticipant;
import com.example.school.participants.assignedparticipants.AssignedRepo;
import com.example.school.scoring.entity.ParticipantAsana;
import com.example.school.scoring.entity.Scoring;
import com.example.school.scoring.request.AsanaScoreRequest;
import com.example.school.scoring.request.JuryMarkRequest;
import com.example.school.scoring.request.ParticipantScoreRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private AssignedGroupRepository assignedGroupRepository;
    @Autowired
    private AssignedRepo assignedRepo;


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
            scoring.setCategory(p.getCategory());

            scoring.setJuryId(p.getJuryId());
            scoring = participantEventRepo.save(scoring);
            List<AssignedParticipant> assignedParticipants = assignedRepo .findAllByAssignedGroupIdAndJuryId(p.getAssignId(), p.getJuryId());
            assignedParticipants.forEach(ap -> { ap.setScored(true);
                assignedRepo.save(ap); });
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
//    public Map<String, Object> getGroupedParticipantScores(Long eventId) {
//
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("eventId", eventId);
//
//        // 1️⃣ Fetch all scorings for this event
//        List<Scoring> scorings = participantEventRepo.findByEventId(eventId);
//
//        // 2️⃣ Get all scoring IDs
//        List<Long> scoringIds = scorings.stream().map(Scoring::getId).toList();
//
//        // 3️⃣ Fetch all ParticipantAsana for these scorings
//        List<ParticipantAsana> asanas = participantAsanaRepo.findAllByScoringIdIn(scoringIds);
//
//        // 4️⃣ Group scorings by participant
//        Map<Long, List<Scoring>> scoringsByParticipant =
//                scorings.stream().collect(Collectors.groupingBy(Scoring::getParticipantId));
//
//        // 5️⃣ Build participant list
//        List<Map<String, Object>> participantsList = new ArrayList<>();
//
//        for (Map.Entry<Long, List<Scoring>> entry : scoringsByParticipant.entrySet()) {
//
//            Long participantId = entry.getKey();
//            List<Scoring> participantScorings = entry.getValue();
//
//            // ⭐ Group scorings by category
//            Map<String, List<Scoring>> scoringsByCategory = participantScorings.stream()
//                    .collect(Collectors.groupingBy(Scoring::getCategory));
//
//            // ⭐ Build category list
//            List<Map<String, Object>> categoryList = new ArrayList<>();
//
//            for (Map.Entry<String, List<Scoring>> catEntry : scoringsByCategory.entrySet()) {
//
//                String category = catEntry.getKey();
//                List<Scoring> categoryScorings = catEntry.getValue();
//
//                // ⭐ Group asanas inside each category
//                Map<String, List<Map<String, Object>>> asanasMap = new LinkedHashMap<>();
//
//                for (Scoring s : categoryScorings) {
//
//                    List<ParticipantAsana> participantAsanas = asanas.stream()
//                            .filter(a -> a.getScoringId().equals(s.getId()))
//                            .toList();
//
//                    for (ParticipantAsana a : participantAsanas) {
//                        asanasMap.computeIfAbsent(a.getAsanaName(), k -> new ArrayList<>())
//                                .add(Map.of(
//                                        "juryId", s.getJuryId(),
//                                        "score", a.getScore()
//                                ));
//                    }
//                }
//
//                // Convert map → list
//                List<Map<String, Object>> asanasList = asanasMap.entrySet().stream()
//                        .map(e -> Map.of(
//                                "asanaName", e.getKey(),
//                                "juryMarks", e.getValue()
//                        ))
//                        .toList();
//
//                // Build category object
//                Map<String, Object> categoryBlock = new LinkedHashMap<>();
//                categoryBlock.put("category", category);
//                categoryBlock.put("asanas", asanasList);
//
//                categoryList.add(categoryBlock);
//            }
//
//            // ⭐ Build final participant response
//            Map<String, Object> participantMap = new LinkedHashMap<>();
//            participantMap.put("participantId", participantId);
//            participantMap.put("categories", categoryList);
//
//            participantsList.add(participantMap);
//        }
//
//        response.put("participants", participantsList);
//        return response;
//    }
//public Map<String, Object> getGroupedParticipantScores(Long eventId) {
//
//
//    Map<String, Object> response = new LinkedHashMap<>();
//    response.put("eventId", eventId);
//
//// 1️⃣ Fetch all scorings for this event
//    List<Scoring> scorings = participantEventRepo.findByEventId(eventId);
//
//// 2️⃣ Get all scoring IDs
//    List<Long> scoringIds = scorings.stream().map(Scoring::getId).toList();
//
//// 3️⃣ Fetch all ParticipantAsana for these scorings
//    List<ParticipantAsana> asanas = participantAsanaRepo.findAllByScoringIdIn(scoringIds);
//
//// 4️⃣ Group scorings by participant
//    Map<Long, List<Scoring>> scoringsByParticipant =
//            scorings.stream().collect(Collectors.groupingBy(Scoring::getParticipantId));
//
//// 5️⃣ Build participant list
//    List<Map<String, Object>> participantsList = new ArrayList<>();
//
//    for (Map.Entry<Long, List<Scoring>> entry : scoringsByParticipant.entrySet()) {
//
//        Long participantId = entry.getKey();
//        List<Scoring> participantScorings = entry.getValue();
//
//        // ⭐ Group scorings by category safely
//        Map<String, List<Scoring>> scoringsByCategory = participantScorings.stream()
//                .collect(Collectors.groupingBy(s -> s.getCategory() != null ? s.getCategory() : "Uncategorized"));
//
//        // ⭐ Build category list
//        List<Map<String, Object>> categoryList = new ArrayList<>();
//
//        for (Map.Entry<String, List<Scoring>> catEntry : scoringsByCategory.entrySet()) {
//
//            String category = catEntry.getKey();
//            List<Scoring> categoryScorings = catEntry.getValue();
//
//            // ⭐ Group asanas inside each category safely
//            Map<String, List<Map<String, Object>>> asanasMap = new LinkedHashMap<>();
//
//            for (Scoring s : categoryScorings) {
//
//                List<ParticipantAsana> participantAsanas = asanas.stream()
//                        .filter(a -> a.getScoringId().equals(s.getId()))
//                        .toList();
//
//                for (ParticipantAsana a : participantAsanas) {
//                    String asanaName = a.getAsanaName() != null ? a.getAsanaName() : "Unnamed Asana";
//
//                    asanasMap.computeIfAbsent(asanaName, k -> new ArrayList<>())
//                            .add(Map.of(
//                                    "juryId", s.getJuryId(),
//                                    "score", a.getScore()
//                            ));
//                }
//            }
//
//            // Convert map → list
//            List<Map<String, Object>> asanasList = asanasMap.entrySet().stream()
//                    .map(e -> Map.of(
//                            "asanaName", e.getKey(),
//                            "juryMarks", e.getValue()
//                    ))
//                    .toList();
//
//            // Build category object
//            Map<String, Object> categoryBlock = new LinkedHashMap<>();
//            categoryBlock.put("category", category);
//            categoryBlock.put("asanas", asanasList);
//
//            categoryList.add(categoryBlock);
//        }
//
//        // ⭐ Build final participant response
//        Map<String, Object> participantMap = new LinkedHashMap<>();
//        participantMap.put("participantId", participantId);
//        participantMap.put("categories", categoryList);
//
//        participantsList.add(participantMap);
//    }
//
//    response.put("participants", participantsList);
//    return response;
//
//
//}latest/
public Map<String, Object> getGroupedParticipantScores(Long eventId) {


    Map<String, Object> response = new LinkedHashMap<>();
    response.put("eventId", eventId);

// 1️⃣ Fetch all scorings for this event
    List<Scoring> scorings = participantEventRepo.findByEventId(eventId);

// 2️⃣ Get all scoring IDs
    List<Long> scoringIds = scorings.stream().map(Scoring::getId).toList();

// 3️⃣ Fetch all ParticipantAsana for these scorings
    List<ParticipantAsana> asanas = participantAsanaRepo.findAllByScoringIdIn(scoringIds);

// 4️⃣ Fetch participants info
    Set<Long> participantIds = scorings.stream()
            .map(Scoring::getParticipantId)
            .collect(Collectors.toSet());
    Map<Long, Participants> participantMap = participantRepository.findAllById(participantIds)
            .stream().collect(Collectors.toMap(Participants::getId, p -> p));

// 5️⃣ Group scorings by participant
    Map<Long, List<Scoring>> scoringsByParticipant =
            scorings.stream().collect(Collectors.groupingBy(Scoring::getParticipantId));

    List<Map<String, Object>> participantsList = new ArrayList<>();

    for (Map.Entry<Long, List<Scoring>> entry : scoringsByParticipant.entrySet()) {
        Long participantId = entry.getKey();
        List<Scoring> participantScorings = entry.getValue();

        Participants participant = participantMap.get(participantId);

        // Group scorings by category safely
        Map<String, List<Scoring>> scoringsByCategory = participantScorings.stream()
                .collect(Collectors.groupingBy(s -> s.getCategory() != null ? s.getCategory() : "Uncategorized"));

        List<Map<String, Object>> categoryList = new ArrayList<>();

        for (Map.Entry<String, List<Scoring>> catEntry : scoringsByCategory.entrySet()) {
            String category = catEntry.getKey();
            List<Scoring> categoryScorings = catEntry.getValue();

            Map<String, List<Map<String, Object>>> asanasMap = new LinkedHashMap<>();
            double categoryGrandTotal = 0.0;

            for (Scoring s : categoryScorings) {
                List<ParticipantAsana> participantAsanas = asanas.stream()
                        .filter(a -> a.getScoringId().equals(s.getId()))
                        .toList();

                for (ParticipantAsana a : participantAsanas) {
                    String asanaName = a.getAsanaName() != null ? a.getAsanaName() : "Unnamed Asana";
                    double score = Double.parseDouble(a.getScore());
                    categoryGrandTotal += score;

                    asanasMap.computeIfAbsent(asanaName, k -> new ArrayList<>())
                            .add(Map.of(
                                    "juryId", s.getJuryId(),
                                    "score", score
                            ));
                }
            }

            List<Map<String, Object>> asanasList = asanasMap.entrySet().stream()
                    .map(e -> {
                        double subtotal = e.getValue().stream()
                                .mapToDouble(v -> (Double) v.get("score"))
                                .sum();
                        return Map.of(
                                "asanaName", e.getKey(),
                                "subtotal", subtotal,
                                "juryMarks", e.getValue()
                        );
                    }).toList();

            Map<String, Object> categoryBlock = new LinkedHashMap<>();
            categoryBlock.put("category", category);
            categoryBlock.put("grandTotal", categoryGrandTotal);
            categoryBlock.put("asanas", asanasList);

            categoryList.add(categoryBlock);
        }

        Map<String, Object> participantMapResponse = new LinkedHashMap<>();
        participantMapResponse.put("participantId", participantId);
        participantMapResponse.put("participantName", participant != null ? participant.getParticipantName(): null);
        participantMapResponse.put("groupName", participant != null ? participant.getGroupName() : null);
        participantMapResponse.put("schoolName", participant != null ? participant.getSchoolName() : null);
        participantMapResponse.put("age",participant!=null?participant.getAge():null);
        participantMapResponse.put("gender",participant!=null?participant.getGender():null);
        participantMapResponse.put("participantCode",participant!=null?participant.getParticipantCode():null);
        participantMapResponse.put("categories", categoryList);

        participantsList.add(participantMapResponse);
    }

    response.put("participants", participantsList);
    return response;


}


//    public Map<String, Object> getGroupedParticipantScores(Long eventId) {
//
//
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("eventId", eventId);
//
//// 1️⃣ Fetch all scorings for this event
//        List<Scoring> scorings = participantEventRepo.findByEventId(eventId);
//
//// 2️⃣ Get all scoring IDs
//        List<Long> scoringIds = scorings.stream().map(Scoring::getId).toList();
//
//// 3️⃣ Fetch all ParticipantAsana for these scorings
//        List<ParticipantAsana> asanas = participantAsanaRepo.findAllByScoringIdIn(scoringIds);
//
//// 4️⃣ Group scorings by participant
//        Map<Long, List<Scoring>> scoringsByParticipant = scorings.stream()
//                .collect(Collectors.groupingBy(Scoring::getParticipantId));
//
//// 5️⃣ Build participant list
//        List<Map<String, Object>> participantsList = new ArrayList<>();
//        for (Map.Entry<Long, List<Scoring>> entry : scoringsByParticipant.entrySet()) {
//            Long participantId = entry.getKey();
//            List<Scoring> participantScorings = entry.getValue();
//
//            // Take category from first scoring
//            String category = participantScorings.isEmpty() ? null : participantScorings.get(0).getCategory();
//
//            // Group asanas by name and collect jury marks
//            Map<String, List<Map<String, Object>>> asanasMap = new LinkedHashMap<>();
//            for (Scoring s : participantScorings) {
//                List<ParticipantAsana> participantAsanas = asanas.stream()
//                        .filter(a -> a.getScoringId().equals(s.getId()))
//                        .toList();
//                for (ParticipantAsana a : participantAsanas) {
//                    asanasMap.computeIfAbsent(a.getAsanaName(), k -> new ArrayList<>())
//                            .add(Map.of(
//                                    "juryId", s.getJuryId(),
//                                    "mark", a.getScore()
//                            ));
//                }
//            }
//
//            // Convert map to list of asanas with jury marks
//            List<Map<String, Object>> asanasList = asanasMap.entrySet().stream()
//                    .map(e -> Map.of(
//                            "asanaName", e.getKey(),
//                            "juryMarks", e.getValue()
//                    ))
//                    .toList();
//
//            Map<String, Object> participantMap = new LinkedHashMap<>();
//            participantMap.put("participantId", participantId);
//            participantMap.put("category", category);
//            participantMap.put("asanas", asanasList);
//
//            participantsList.add(participantMap);
//        }
//
//        response.put("participants", participantsList);
//        return response;
//
//
//    }

public Map<String, Object> getScoresByEventAndParticipant(Long eventId, Long participantId) {


// Fetch all scorings for this participant in the event
    List<Scoring> scorings = participantEventRepo.findByEventIdAndParticipantIdAndDeletedFalse(eventId, participantId);

    if (scorings == null || scorings.isEmpty()) {
        throw new RuntimeException("No score found for participant " + participantId + " in event " + eventId);
    }

// Fetch participant details
    Participants participant = participantRepository.findById(participantId).orElse(null);

// Fetch all ParticipantAsana for these scorings
    List<Long> scoringIds = scorings.stream().map(Scoring::getId).toList();
    List<ParticipantAsana> asanas = participantAsanaRepo.findAllByScoringIdIn(scoringIds);
    Set<Long> juryIds = scorings.stream().map(Scoring::getJuryId).collect(Collectors.toSet());
    List<Jury> juries = juryRepository.findAllById(juryIds);
    Map<Long, String> juryMap = juries.stream()
            .collect(Collectors.toMap(Jury::getId, Jury::getName));
// Group scorings by category
    Map<String, List<Scoring>> scoringsByCategory = scorings.stream()
            .collect(Collectors.groupingBy(s -> s.getCategory() != null ? s.getCategory() : "Uncategorized"));

    List<Map<String, Object>> categoryList = new ArrayList<>();

    for (Map.Entry<String, List<Scoring>> catEntry : scoringsByCategory.entrySet()) {
        String category = catEntry.getKey();
        List<Scoring> categoryScorings = catEntry.getValue();

        Map<String, List<Map<String, Object>>> asanasMap = new LinkedHashMap<>();
        double categoryGrandTotal = 0.0;

        for (Scoring s : categoryScorings) {
            List<ParticipantAsana> participantAsanas = asanas.stream()
                    .filter(a -> a.getScoringId().equals(s.getId()))
                    .toList();

            for (ParticipantAsana a : participantAsanas) {
                String asanaName = a.getAsanaName() != null ? a.getAsanaName() : "Unnamed Asana";
                double score = Double.parseDouble(a.getScore());
                categoryGrandTotal += score;
                Long juryId = s.getJuryId();
                String juryName = juryMap.get(juryId); // get jury name
                asanasMap.computeIfAbsent(asanaName, k -> new ArrayList<>())
                        .add(Map.of(
                                "juryId", s.getJuryId(),
                                "juryName", juryName,
                                "score", score
                        ));
            }
        }

        List<Map<String, Object>> asanasList = asanasMap.entrySet().stream()
                .map(e -> {
                    double subtotal = e.getValue().stream()
                            .mapToDouble(v -> (Double) v.get("score"))
                            .sum();
                    return Map.of(
                            "asanaName", e.getKey(),
                            "subtotal", subtotal,
                            "juryMarks", e.getValue()
                    );
                }).toList();

        Map<String, Object> categoryBlock = new LinkedHashMap<>();
        categoryBlock.put("category", category);
        categoryBlock.put("grandTotal", categoryGrandTotal);
        categoryBlock.put("asanas", asanasList);

        categoryList.add(categoryBlock);
    }

    Map<String, Object> participantMapResponse = new LinkedHashMap<>();
    participantMapResponse.put("participantId", participantId);
    participantMapResponse.put("participantName", participant != null ? participant.getParticipantName() : null);
    participantMapResponse.put("groupName", participant != null ? participant.getGroupName() : null);
    participantMapResponse.put("schoolName", participant != null ? participant.getSchoolName() : null);
    participantMapResponse.put("age",participant!=null?participant.getAge():null);
    participantMapResponse.put("gender",participant!=null?participant.getGender():null);
    participantMapResponse.put("participantCode",participant!=null?participant.getParticipantCode():null);
    participantMapResponse.put("address",participant!=null?participant.getAddress():null);
    participantMapResponse.put("dateOfBirth",participant!=null?participant.getDateOfBirth():null);
    participantMapResponse.put("yogaMasterName",participant!=null?participant.getYogaMasterName():null);
    participantMapResponse.put("yogaMasterContact",participant!=null?participant.getYogaMasterContact():null);


    participantMapResponse.put("categories", categoryList);

    Map<String, Object> response = new LinkedHashMap<>();
    response.put("eventId", eventId);
    response.putAll(participantMapResponse);

    return response;


}

//    public Map<String, Object> getScoresByEventAndParticipant(Long eventId, Long participantId) {
//
//        // Fetch scoring row for this event + participant
//        Scoring scoring =
//                participantEventRepo.findByEventIdAndParticipantIdAndDeletedFalse(eventId, participantId);
//
//        if (scoring == null) {
//            throw new RuntimeException("No score found for participant " + participantId + " in event " + eventId);
//        }
//
//        Map<String, Object> participantMap = new HashMap<>();
//        participantMap.put("participantId", scoring.getParticipantId());
//        participantMap.put("grandTotal", scoring.getGrandTotal());
//        participantMap.put("juryId", scoring.getJuryId());
//
//        // Fetch all asana scores
//        List<ParticipantAsana> asanas =
//                participantAsanaRepo.findByScoringIdAndDeletedFalse(scoring.getId());
//
//        List<Map<String, Object>> asanaList = new ArrayList<>();
//
//        for (ParticipantAsana asana : asanas) {
//            Map<String, Object> asanaMap = new HashMap<>();
//            asanaMap.put("asanaName", asana.getAsanaName());
//            asanaMap.put("score", asana.getScore());
//            asanaList.add(asanaMap);
//        }
//
//        participantMap.put("asanas", asanaList);
//
//        // Final response format
//        Map<String, Object> response = new HashMap<>();
//        response.put("eventId", eventId);
//        response.put("scoreOfParticipant", participantMap);
//
//        return response;
//    }


}





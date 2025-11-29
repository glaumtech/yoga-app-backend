package com.example.school.participants.assignedparticipants;

import com.example.school.jury.Jury;
import com.example.school.jury.JuryDto;
import com.example.school.jury.JuryRepository;
import com.example.school.participants.PageFilterRequest;
import com.example.school.participants.ParticipantRep;
import com.example.school.participants.Participants;
import com.example.school.team.Team;
import com.example.school.team.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssignedService {
    @Autowired
    private AssignedRepo assignedRepo;

    @Autowired
    private AssignedGroupRepository assignedGroupRepo;

    @Autowired
    private ParticipantRep participantRep;
    @Autowired
    private JuryRepository juryRep;
    @Autowired
    private TeamRepository teamRepository;



@Transactional
public List<AssignedParticipant> assignParticipants(RequestDto req) {

    if (req.getParticipants() == null || req.getParticipants().isEmpty()) {
        throw new RuntimeException("No participants provided");
    }
    if (req.getJuryDtos() == null || req.getJuryDtos().isEmpty()) {
        throw new RuntimeException("No juries provided");
    }

//// 1️⃣ Get or create parent group
//    AssignedGroup group = assignedGroupRepo.findByEventIdAndTeamId(req.getEventId(), req.getTeamId())
//            .orElseGet(() -> {
//                AssignedGroup newGroup = new AssignedGroup();
//                newGroup.setEventId(req.getEventId());
//                newGroup.setTeamId(req.getTeamId());
//                return assignedGroupRepo.save(newGroup);
//            });
    // 1️⃣ Get or create parent group

    AssignedGroup newGroup = new AssignedGroup();
    newGroup.setEventId(req.getEventId());
    newGroup.setTeamId(req.getTeamId());
    AssignedGroup group = assignedGroupRepo.save(newGroup);


// 2️⃣ Validate participant and jury IDs
    Set<Long> validParticipantIds = participantRep.findAllById(
            req.getParticipants().stream().map(ParticipantsDto::getId).toList()
    ).stream().map(Participants::getId).collect(Collectors.toSet());

    Set<Long> validJuryIds = juryRep.findAllById(
            req.getJuryDtos().stream().map(JuryDto::getId).toList()
    ).stream().map(Jury::getId).collect(Collectors.toSet());

    List<Long> invalidParticipantIds = req.getParticipants().stream()
            .map(ParticipantsDto::getId)
            .filter(id -> !validParticipantIds.contains(id))
            .toList();

    List<Long> invalidJuryIds = req.getJuryDtos().stream()
            .map(JuryDto::getId)
            .filter(id -> !validJuryIds.contains(id))
            .toList();

    if (!invalidParticipantIds.isEmpty() || !invalidJuryIds.isEmpty()) {
        throw new RuntimeException("Invalid IDs: Participants=" + invalidParticipantIds + ", Juries=" + invalidJuryIds);
    }

// 3️⃣ Fetch existing assignments for this event (exclude deleted)
    Map<Long, Set<String>> participantCategoryMap = assignedRepo
            .findByEventId(req.getEventId()) // make sure query filters deleted=false
            .stream()
            .collect(Collectors.groupingBy(
                    AssignedParticipant::getParticipantId,
                    Collectors.mapping(AssignedParticipant::getCategory, Collectors.toSet())
            ));

    System.out.println("Participant Category Map: " + participantCategoryMap);

// 4️⃣ Prepare to save new assignments
    List<AssignedParticipant> savedParticipants = new ArrayList<>();

    Map<String, Set<Long>> duplicates = new HashMap<>();

    for (JuryDto jury : req.getJuryDtos()) {
    for (ParticipantsDto participant : req.getParticipants()) {
        Set<String> assignedCategories = participantCategoryMap.getOrDefault(participant.getId(), new HashSet<>());


        if (assignedCategories.stream().anyMatch(c -> c.trim().equalsIgnoreCase(req.getCategory().trim()))) { duplicates .computeIfAbsent(req.getCategory(), k -> new HashSet<>()) .add(participant.getId()); continue; // skip adding new assignment for this participant
        }



            AssignedParticipant child = new AssignedParticipant();
            child.setAssignedGroupId(group.getId());
            child.setParticipantId(participant.getId());
            child.setJuryId(jury.getId());
            child.setCategory(req.getCategory());
            child.setStatus("Assigned");

            savedParticipants.add(child);
        }
    }


    if (!duplicates.isEmpty()) {
        List messages = duplicates.entrySet().stream()
                .map(e -> "Category '" + e.getKey() + "' is already assigned for participant(s) " +
                        e.getValue().stream().sorted().map(String::valueOf).collect(Collectors.joining(",")))
                .toList();
        throw new RuntimeException(String.join("; ", messages));
    }
// 6️⃣ Save all new assignments at once
    return assignedRepo.saveAll(savedParticipants);


}

    public Map<String, Object> getParticipantsAndJuriesByEvent(Long eventId, PageFilterRequest request) {
        Pageable pageable = request.toPageable();

        // Fetch assigned groups (paginated)
        Page<AssignedGroup> assignedGroupsPage = assignedGroupRepo.findAllByEventId(eventId, pageable);

        List<Long> groupIds = assignedGroupsPage.stream().map(AssignedGroup::getId).toList();

        // Fetch all assignments for these groups
        List<AssignedParticipant> assignedParticipants = assignedRepo.findAllByAssignedGroupIdIn(groupIds);

        // Fetch all participants and map by ID
        Set<Long> participantIds = assignedParticipants.stream()
                .map(AssignedParticipant::getParticipantId)
                .collect(Collectors.toSet());
        Map<Long, Participants> participantMap = participantRep.findAllById(participantIds)
                .stream().collect(Collectors.toMap(Participants::getId, p -> p));

        // Fetch all juries and map by group
        Set<Long> juryIds = assignedParticipants.stream()
                .map(AssignedParticipant::getJuryId)
                .collect(Collectors.toSet());
        Map<Long, Jury> juryMap = juryRep.findAllById(juryIds)
                .stream().collect(Collectors.toMap(Jury::getId, j -> j));
        String category = assignedParticipants.stream()
                .map(AssignedParticipant::getCategory)
                .findFirst()
                .orElse(null); // or throw exception if required
        // Build response per group
        List<Map<String, Object>> groupResponses = assignedGroupsPage.stream().map(group -> {
            Map<String, Object> groupData = new LinkedHashMap<>();
            groupData.put("assignedId", group.getId());

            groupData.put("teamId", group.getTeamId());
            Team team = teamRepository.findById(group.getTeamId()).orElse(null);
            groupData.put("teamName", team != null ? team.getName() : null);
            groupData.put("category",category);


            // Filter participants for this group
            List<ParticipantsDto> participants = assignedParticipants.stream()
                    .filter(ap -> ap.getAssignedGroupId().equals(group.getId()))
                    .collect(Collectors.groupingBy(ap -> ap.getParticipantId() + "_" + ap.getCategory()))
                    .values().stream()
                    .map(list -> {
                        AssignedParticipant ap = list.get(0);
                        Participants p = participantMap.get(ap.getParticipantId());
                        return new ParticipantsDto(
                                p.getId(),
                                p.getParticipantName(),
                                p.getGroupName(),
                                p.getSchoolName()

                        );
                    })
                    .toList();
            groupData.put("participants", participants);

            // Filter juries for this group
            List<JuryDto> juries = assignedParticipants.stream()
                    .filter(ap -> ap.getAssignedGroupId().equals(group.getId()))
                    .map(AssignedParticipant::getJuryId)
                    .distinct()
                    .map(juryMap::get)
                    .map(j -> new JuryDto(j.getId(), j.getName()))
                    .toList();
            groupData.put("juries", juries);

            return groupData;
        }).toList();

        // Prepare final response
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("groups", groupResponses);
        data.put("currentPage", assignedGroupsPage.getNumber());
        data.put("totalPages", assignedGroupsPage.getTotalPages());
        data.put("totalElements", assignedGroupsPage.getTotalElements());
        data.put("pageSize", assignedGroupsPage.getSize());

        return data;
    }






    public Map<String, Object> getParticipantsForJury(Long eventId, Long juryId) {
        // 1️⃣ Fetch all assigned groups for the event
        if (juryId == null) {
            throw new RuntimeException("Jury ID is required to fetch participants for this event.");
        }
        List<AssignedGroup> assignedGroups = assignedGroupRepo.findAllByEventId(eventId);

        // 2️⃣ Extract all group IDs to fetch participants
        List<Long> groupIds = assignedGroups.stream().map(AssignedGroup::getId).toList();

        // 3️⃣ Fetch assigned participants filtered by juryId if provided
//        List<AssignedParticipant> assignedParticipants;
//        if (juryId != null) {
//            assignedParticipants = assignedRepo.findAllByAssignedGroupIdInAndJuryId(groupIds, juryId);
//        } else {
//            assignedParticipants = assignedRepo.findAllByAssignedGroupIdIn(groupIds);
//        }
        List<AssignedParticipant> assignedParticipants =
                assignedRepo.findAllByAssignedGroupIdInAndJuryId(groupIds, juryId);
        if (assignedParticipants.isEmpty()) {
            throw new RuntimeException("No participants assigned for eventId=" + eventId + " and juryId=" + juryId);
        }

        // 4️⃣ Fetch participant and jury details
        Set<Long> participantIds = assignedParticipants.stream()
                .map(AssignedParticipant::getParticipantId)
                .collect(Collectors.toSet());
        Set<Long> juryIds = assignedParticipants.stream()
                .map(AssignedParticipant::getJuryId)
                .collect(Collectors.toSet());

        Map<Long, Participants> participantMap = participantRep.findAllById(participantIds)
                .stream().collect(Collectors.toMap(Participants::getId, p -> p));
        Map<Long, Jury> juryMap = juryRep.findAllById(juryIds)
                .stream().collect(Collectors.toMap(Jury::getId, j -> j));

        // 5️⃣ Group participants by assignedGroupId
        Map<Long, List<AssignedParticipant>> participantsByGroup = assignedParticipants.stream()
                .collect(Collectors.groupingBy(AssignedParticipant::getAssignedGroupId));

        // 6️⃣ Build groups list
        List<Map<String, Object>> groups = new ArrayList<>();
        for (AssignedGroup group : assignedGroups) {
            Map<String, Object> groupData = new LinkedHashMap<>();
            groupData.put("assignedId", group.getId());
            groupData.put("teamId", group.getTeamId());
            groupData.put("teamName", "Team " + group.getTeamId()); // replace with actual name if available

            // Category (from first participant in the group)
            List<AssignedParticipant> groupParticipants = participantsByGroup.getOrDefault(group.getId(), List.of());
            String groupCategory = groupParticipants.isEmpty() ? null : groupParticipants.get(0).getCategory();
            groupData.put("category", groupCategory);

            // Participants for this group (distinct by participant ID)
            List<ParticipantsDto> participantResponses = groupParticipants.stream()
                    .map(ap -> participantMap.get(ap.getParticipantId()))
                    .filter(Objects::nonNull)
                    .distinct() // remove duplicates
                    .map(p -> new ParticipantsDto(
                            p.getId(),
                            p.getParticipantName(),
                            p.getGroupName(),
                            p.getSchoolName()
                    ))
                    .toList();
            groupData.put("participants", participantResponses);

            // Juries for this group (distinct)
            List<JuryDto> juryResponses = groupParticipants.stream()
                    .map(ap -> juryMap.get(ap.getJuryId()))
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(j -> new JuryDto(j.getId(), j.getName()))
                    .toList();
            groupData.put("juries", juryResponses);

            groups.add(groupData);
        }

        // 7️⃣ Return response
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("groups", groups);



        return data;
    }


//
//    public Map<String, Object> getParticipantsForJury(Long eventId, Long juryId) {
//        // 1️⃣ Fetch all assigned groups for the event
//        List<AssignedGroup> assignedGroups = assignedGroupRepo.findAllByEventId(eventId);
//
//        // 2️⃣ Extract all group IDs to fetch participants
//        List<Long> groupIds = assignedGroups.stream().map(AssignedGroup::getId).toList();
//
//        // 3️⃣ Fetch assigned participants filtered by juryId if provided
//        List<AssignedParticipant> assignedParticipants;
//        if (juryId != null) {
//            assignedParticipants = assignedRepo.findAllByAssignedGroupIdInAndJuryId(groupIds, juryId);
//        } else {
//            assignedParticipants = assignedRepo.findAllByAssignedGroupIdIn(groupIds);
//        }
//
//        // 4️⃣ Fetch participant and jury details
//        Set<Long> participantIds = assignedParticipants.stream()
//                .map(AssignedParticipant::getParticipantId)
//                .collect(Collectors.toSet());
//        Set<Long> juryIds = assignedParticipants.stream()
//                .map(AssignedParticipant::getJuryId)
//                .collect(Collectors.toSet());
//
//        Map<Long, Participants> participantMap = participantRep.findAllById(participantIds)
//                .stream().collect(Collectors.toMap(Participants::getId, p -> p));
//        Map<Long, Jury> juryMap = juryRep.findAllById(juryIds)
//                .stream().collect(Collectors.toMap(Jury::getId, j -> j));
//
//        // 5️⃣ Group participants by assignedGroupId
//        Map<Long, List<AssignedParticipant>> participantsByGroup = assignedParticipants.stream()
//                .collect(Collectors.groupingBy(AssignedParticipant::getAssignedGroupId));
//
//        // 6️⃣ Build groups list
//        List<Map<String, Object>> groups = new ArrayList<>();
//        for (AssignedGroup group : assignedGroups) {
//            Map<String, Object> groupData = new LinkedHashMap<>();
//            groupData.put("assignedId", group.getId());
//            groupData.put("teamId", group.getTeamId());
//            groupData.put("teamName", "Team " + group.getTeamId()); // replace with actual name if available
//
//            // Category (from first participant in the group)
//            List<AssignedParticipant> groupParticipants = participantsByGroup.getOrDefault(group.getId(), List.of());
//            String groupCategory = groupParticipants.isEmpty() ? null : groupParticipants.get(0).getCategory();
//            groupData.put("category", groupCategory);
//
//            // Participants for this group
//            List<ParticipantsDto> participantResponses = groupParticipants.stream()
//                    .map(ap -> {
//                        Participants p = participantMap.get(ap.getParticipantId());
//                        return new ParticipantsDto(
//                                p.getId(),
//                                p.getParticipantName(),
//                                p.getGroupName(),
//                                p.getSchoolName()
//                        );
//                    }).toList();
//            groupData.put("participants", participantResponses);
//
//            // Juries for this group (distinct)
//            List<JuryDto> juryResponses = groupParticipants.stream()
//                    .map(ap -> juryMap.get(ap.getJuryId()))
//                    .filter(Objects::nonNull)
//                    .distinct()
//                    .map(j -> new JuryDto(j.getId(), j.getName()))
//                    .toList();
//            groupData.put("juries", juryResponses);
//
//            groups.add(groupData);
//        }
//
//        // 7️⃣ Return response
//        Map<String, Object> data = new LinkedHashMap<>();
//        data.put("groups", groups);
//
////        Map<String, Object> response = new HashMap<>();
////        response.put("data", data);
////        response.put("message", "Participants retrieved successfully!");
////        response.put("status", "success");
//
//        return data;
//    }



//public List<ParticipantRequest> getParticipantsForJury(Long eventId, Long juryId) {
//    return assignedRepo.findParticipantsByEventAndJury(eventId, juryId);
//}

//
//        public Map<String, Object> getParticipantsAndJuriesByEvent(Long eventId, PageFilterRequest request) {
//            Pageable pageable = request.toPageable();
//
//            // Fetch assigned participants
//            Page<AssignedParticipant> assignedParticipantsPage = assignedRepo.findAllByEventId(eventId, pageable);
//
//            // Extract participant and jury IDs
//            Set<Long> participantIds = assignedParticipantsPage.stream()
//                    .map(AssignedParticipant::getParticipantId)
//                    .collect(Collectors.toSet());
//
//            Set<Long> juryIds = assignedParticipantsPage.stream()
//                    .map(AssignedParticipant::getJuryId)
//                    .collect(Collectors.toSet());
//
//            // Fetch participant and jury details
//            //List<Participants> participants = participantRep.findAllById(participantIds);
//            List<Jury> juries = juryRep.findAllById(juryIds);
//
////            // Map to response DTOs
////            List<ParticipantsDto> participantResponses = participants.stream()
////                    .map(p -> new ParticipantsDto(p.getId(), p.getParticipantName(),p.getGroupName(),ap))
////                    .toList();
//            Map<Long, Participants> participantMap = participantRep.findAllById(participantIds)
//                    .stream().collect(Collectors.toMap(Participants::getId, p -> p));
//            List<ParticipantsDto> participantResponses = assignedParticipantsPage.stream()
//                    .map(ap -> {
//                        Participants p = participantMap.get(ap.getParticipantId());
//                        return new ParticipantsDto(
//                                p.getId(),
//                                p.getParticipantName(),
//                                p.getGroupName(),
//                                ap.getCategory() // <-- category from AssignedParticipant
//                        );
//                    }).toList();
//            List<JuryDto> juryResponses = juries.stream()
//                    .map(j -> new JuryDto(j.getId(), j.getName()))
//                    .toList();
//
//            // Only return “data” and pagination info in a map
//            Map<String, Object> data = new HashMap<>();
//            data.put("participants", participantResponses);
//            data.put("juries", juryResponses);
//            data.put("currentPage", assignedParticipantsPage.getNumber());
//            data.put("totalPages", assignedParticipantsPage.getTotalPages());
//            data.put("totalElements", assignedParticipantsPage.getTotalElements());
//            data.put("pageSize", assignedParticipantsPage.getSize());
//
//            return data;
//    }
//




//public List<AssignedParticipant> assignParticipants(RequestDto req) {
//
//
//    List<ParticipantRequest> participantsToAssign = new ArrayList<>();
//    List<AssignedParticipant> savedAssignments = new ArrayList<>();
//
//    // 1️⃣ First, filter out already assigned participants
//    for (ParticipantRequest participant : req.getParticipants()) {
//        boolean alreadyAssigned = assignedRepo.existsByEventIdAndCategoryAndParticipantId(
//                req.getEventId(),
//                req.getCategory(),
//                participant.getId()
//        );
//
//        if (alreadyAssigned) {
//
//                throw new RuntimeException(
//                        "Participant " + participant.getName() +
//                                " is already assigned in event " + req.getEventId() +
//                                " under category " + req.getCategory()
//                );
//
//
//        } else {
//            participantsToAssign.add(participant);
//        }
//    }
//
//    // 2️⃣ Assign remaining participants to all juries
//    for (JuryDto jury : req.getJuryDtos()) {
//        for (ParticipantRequest participant : participantsToAssign) {
//            AssignedParticipant ap = new AssignedParticipant();
//            ap.setEventId(req.getEventId());
//            ap.setJuryId(jury.getId());
//            ap.setParticipantId(participant.getId());
//            ap.setCategory(req.getCategory());
//
//            AssignedParticipant saved = assignedRepo.save(ap);
//            savedAssignments.add(saved);
//
//            System.out.println("Saved: Participant " + participant.getName() +
//                    " to Jury " + jury.getId());
//        }
//    }
//
//    // Return all saved assignments
//    return savedAssignments;
//}
//
//
//public List<ParticipantRequest> getParticipantsForJury(Long eventId, Long juryId) {
//    return assignedRepo.findParticipantsByEventAndJury(eventId, juryId);
//}
//    public Page<ParticipantRequest> getParticipantsByEvent(Long eventId, PageFilterRequest request) {
//        // Create pageable using request's toPageable() method
//        var pageable = request.toPageable();
//
//        // Fetch participants
//        return assignedRepo.findParticipantsByEvent(eventId, pageable);
//    }

}

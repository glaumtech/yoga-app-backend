package com.example.school.participants.assignedparticipants;

import com.example.school.jury.Jury;
import com.example.school.jury.JuryDto;
import com.example.school.jury.JuryRepository;
import com.example.school.participants.PageFilterRequest;
import com.example.school.participants.ParticipantRep;
import com.example.school.participants.Participants;
import com.example.school.scoring.ScoringRepository;
import com.example.school.scoring.entity.Scoring;
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
    @Autowired
    private ScoringRepository scoringRepo;



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

// 1️⃣ Fetch all assigned groups for the event (paginated)
    Page<AssignedGroup> assignedGroupsPage = assignedGroupRepo.findAllByEventId(eventId, pageable);

    List<Long> groupIds = assignedGroupsPage.stream()
            .map(AssignedGroup::getId)
            .filter(Objects::nonNull) // ensure no null IDs
            .toList();

// 2️⃣ Fetch only unscored participants in these groups
    List<AssignedParticipant> assignedParticipants =
            assignedRepo.findAllByAssignedGroupIdInAndIsScoredFalse(groupIds);

// 3️⃣ Fetch participants info
    Set<Long> participantIds = assignedParticipants.stream()
            .map(AssignedParticipant::getParticipantId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    Map<Long, Participants> participantMap = participantRep.findAllById(participantIds)
            .stream().collect(Collectors.toMap(Participants::getId, p -> p));

// 4️⃣ Fetch jury details
    Set<Long> juryIds = assignedParticipants.stream()
            .map(AssignedParticipant::getJuryId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    Map<Long, Jury> juryMap = juryRep.findAllById(juryIds)
            .stream().collect(Collectors.toMap(Jury::getId, j -> j));

// 5️⃣ Optional: fetch category
//    String category = assignedParticipants.stream()
//            .map(AssignedParticipant::getCategory)
//            .filter(Objects::nonNull)
//            .findFirst()
//            .orElse(null);
// Extract category only from this group's participants




// 6️⃣ Build group response
    List<Map<String, Object>> groupResponses = assignedGroupsPage.stream().map(group -> {
        Map<String, Object> groupData = new LinkedHashMap<>();
        groupData.put("assignedId", group.getId());
        groupData.put("teamId", group.getTeamId());

        Team team = Optional.ofNullable(group.getTeamId())
                .flatMap(teamRepository::findById)
                .orElse(null);
        groupData.put("teamName", team != null ? team.getName() : null);
        String groupCategory = assignedParticipants.stream()
                .filter(ap -> group.getId().equals(ap.getAssignedGroupId()))
                .map(AssignedParticipant::getCategory)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        groupData.put("category", groupCategory);

        // Participants under this group (only unscored)
        List<ParticipantsDto> participants = assignedParticipants.stream()
                .filter(ap -> group.getId().equals(ap.getAssignedGroupId()))
                .map(ap -> participantMap.get(ap.getParticipantId()))
                .filter(Objects::nonNull)
                .distinct()
                .map(p -> new ParticipantsDto(
                        p.getId(),
                        p.getParticipantName(),
                        p.getGroupName(),
                        p.getSchoolName()
                ))
                .toList();
        groupData.put("participants", participants);

        // Juries for this group (distinct, only assigned to unscored participants)
        List<JuryDto> juries = assignedParticipants.stream()
                .filter(ap -> group.getId().equals(ap.getAssignedGroupId()))
                .map(ap -> juryMap.get(ap.getJuryId()))
                .filter(Objects::nonNull)
                .distinct()
                .map(j -> new JuryDto(j.getId(), j.getName()))
                .toList();
        groupData.put("juries", juries);

        return groupData;
    }).toList();

// 7️⃣ Final response
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("groups", groupResponses);
    data.put("currentPage", assignedGroupsPage.getNumber());
    data.put("totalPages", assignedGroupsPage.getTotalPages());
    data.put("totalElements", assignedGroupsPage.getTotalElements());
    data.put("pageSize", assignedGroupsPage.getSize());

    return data;


}


public Map<String, Object> getParticipantsForJury(Long eventId, Long juryId) {
    if (juryId == null) {
        throw new RuntimeException("Jury ID is required to fetch participants for this event.");
    }

// 1️⃣ Fetch all assigned groups for the event
   // List<AssignedGroup> assignedGroups = assignedGroupRepo.findAllByEventId(eventId);
    List<AssignedGroup> assignedGroups = assignedGroupRepo.findAllByEventId(eventId)
            .stream()
            .sorted(Comparator.comparing(AssignedGroup::getId)) // ASC order
            .toList();

    if (assignedGroups.isEmpty()) {

        throw new RuntimeException("No groups found for eventId=" + eventId);
    }

// 2️⃣ Extract group IDs
    List<Long> groupIds = assignedGroups.stream()
            .map(AssignedGroup::getId)
            .filter(Objects::nonNull)
            .toList();

// 3️⃣ Fetch participants assigned to this jury in these groups and who are unscored
    List<AssignedParticipant> assignedParticipants =
            assignedRepo.findAllByAssignedGroupIdInAndJuryIdAndIsScoredFalse(groupIds, juryId);

    if (assignedParticipants.isEmpty()) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("groups", Collections.emptyList());
        return data;
       // throw new RuntimeException("No unscored participants assigned for eventId=" + eventId + " and juryId=" + juryId);
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

// 6️⃣ Build groups list, excluding empty groups
    List<Map<String, Object>> groups = new ArrayList<>();
    for (AssignedGroup group : assignedGroups) {
        List<AssignedParticipant> groupParticipants = participantsByGroup.getOrDefault(group.getId(), List.of());

        // Skip groups with no participants
        if (groupParticipants.isEmpty()) continue;

        Map<String, Object> groupData = new LinkedHashMap<>();
        groupData.put("assignedId", group.getId());
        groupData.put("teamId", group.getTeamId());

        Team team = teamRepository.findById(group.getTeamId()).orElse(null);
        groupData.put("teamName", team != null ? team.getName() : null);

        String groupCategory = groupParticipants.get(0).getCategory();
        groupData.put("category", groupCategory);

        List<ParticipantsDto> participantResponses = groupParticipants.stream()
                .map(ap -> participantMap.get(ap.getParticipantId()))
                .filter(Objects::nonNull)
                .distinct()
                .map(p -> new ParticipantsDto(
                        p.getId(),
                        p.getParticipantName(),
                        p.getGroupName(),
                        p.getSchoolName()
                ))
                .sorted(Comparator.comparing(ParticipantsDto::getId)) // ASC by name
                .toList();
        groupData.put("participants", participantResponses);

        List<JuryDto> juryResponses = groupParticipants.stream()
                .map(ap -> juryMap.get(ap.getJuryId()))
                .filter(Objects::nonNull)
                .distinct()
                .map(j -> new JuryDto(j.getId(), j.getName()))
                .sorted(Comparator.comparing(JuryDto::getId)) // ASC by name
                .toList();
        groupData.put("juries", juryResponses);

        groups.add(groupData);
    }

// 7️⃣ Return response
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("groups", groups);

    return data;


}




}

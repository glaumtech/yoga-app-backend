package com.example.school.participants.assignedparticipants;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface AssignedRepo extends JpaRepository<AssignedParticipant ,Long> {
//    boolean existsByEventIdAndJuryIdAndParticipantId(Long eventId, Long juryId, Long participantId);
//
@Query("""
    SELECT ap FROM AssignedParticipant ap
    JOIN AssignedGroup ag ON ap.assignedGroupId = ag.id
    WHERE ag.eventId = :eventId
""")
List<AssignedParticipant> findAllByEventId(@Param("eventId") Long eventId);

//
//    List<AssignedParticipant> findAllByEventIdAndJuryId(Long eventId, Long juryId);
//    boolean existsByEventId(Long eventId);
//
//    boolean existsByEventIdAndCategoryAndParticipantId(Long eventId, String category, Long participantId);
////    @Query("SELECT new com.example.school.participants.assignedparticipants.ParticipantRequest(p.id, p.participantName) " +
////            "FROM AssignedParticipant ap JOIN Participants p ON ap.participantId = p.id " +
////            "WHERE ap.eventId = :eventId AND ap.juryId = :juryId")
////    List<ParticipantRequest> findParticipantsByEventAndJury(Long eventId, Long juryId);
//
@Query("""
    SELECT new com.example.school.participants.assignedparticipants.ParticipantRequest(p.id, p.participantName)
    FROM AssignedParticipant ap
    JOIN Participants p ON ap.participantId = p.id
    JOIN AssignedGroup ag ON ap.assignedGroupId = ag.id
    WHERE ag.eventId = :eventId
      AND (:juryId IS NULL OR ap.juryId = :juryId)
""")
List<ParticipantRequest> findParticipantsByEventAndJury(
        @Param("eventId") Long eventId,
        @Param("juryId") Long juryId
);

    //    @Query("""
//    SELECT new com.example.school.participants.assignedparticipants.ParticipantRequest(p.id, p.participantName)
//    FROM AssignedParticipant ap
//    JOIN Participants p ON ap.participantId = p.id
//    WHERE ap.eventId = :eventId
//""")
//    Page<ParticipantRequest> findParticipantsByEvent(
//            @Param("eventId") Long eventId,
//            Pageable pageable
//    );
//
//    List<AssignedParticipant> findAllByEventIdAndCategory(Long eventId, String category);
@Query("""
    SELECT ap FROM AssignedParticipant ap
    JOIN AssignedGroup ag ON ap.assignedGroupId = ag.id
    WHERE ag.eventId = :eventId
      AND ap.category = :category
""")
List<AssignedParticipant> findAllByEventIdAndCategory(
        @Param("eventId") Long eventId,
        @Param("category") String category
);

    @Query("""
    SELECT ap FROM AssignedParticipant ap
    JOIN AssignedGroup ag ON ap.assignedGroupId = ag.id
    WHERE ag.eventId = :eventId
    """)
    Page<AssignedParticipant> findAllByEventId(Long eventId, Pageable pageable);

    @Query(value = """
    SELECT ap.* 
    FROM assigned_participant ap
    JOIN assigned_group ag ON ap.assigned_group_id = ag.id
    WHERE ag.event_id = :eventId
""", nativeQuery = true)
    List<AssignedParticipant> findByEventId(@Param("eventId") Long eventId);


    List<AssignedParticipant> findAllByAssignedGroupIdIn(List<Long> groupIds);

    List<AssignedParticipant> findAllByAssignedGroupIdInAndJuryId(List<Long> groupIds, Long juryId);


    List<AssignedParticipant> findAllByAssignedGroupIdInAndIsScoredFalse(List<Long> groupIds);

    List<AssignedParticipant> findAllByAssignedGroupIdInAndJuryIdAndIsScoredFalse(List<Long> groupIds, Long juryId);

    List<AssignedParticipant> findAllByAssignedGroupIdAndJuryId(Long assignId, Long juryId);
}

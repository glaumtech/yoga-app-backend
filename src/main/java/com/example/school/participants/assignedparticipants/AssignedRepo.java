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
    boolean existsByEventIdAndJuryIdAndParticipantId(Long eventId, Long juryId, Long participantId);

    List<AssignedParticipant> findAllByEventId(Long eventId);

    List<AssignedParticipant> findAllByEventIdAndJuryId(Long eventId, Long juryId);
    boolean existsByEventId(Long eventId);

    boolean existsByEventIdAndCategoryAndParticipantId(Long eventId, String category, Long participantId);
//    @Query("SELECT new com.example.school.participants.assignedparticipants.ParticipantRequest(p.id, p.participantName) " +
//            "FROM AssignedParticipant ap JOIN Participants p ON ap.participantId = p.id " +
//            "WHERE ap.eventId = :eventId AND ap.juryId = :juryId")
//    List<ParticipantRequest> findParticipantsByEventAndJury(Long eventId, Long juryId);

    @Query("""
    SELECT new com.example.school.participants.assignedparticipants.ParticipantRequest(p.id, p.participantName)
    FROM AssignedParticipant ap 
    JOIN Participants p ON ap.participantId = p.id
    WHERE ap.eventId = :eventId
      AND (:juryId IS NULL OR ap.juryId = :juryId)
""")
    List<ParticipantRequest> findParticipantsByEventAndJury(
            @Param("eventId") Long eventId,
            @Param("juryId") Long juryId
    );
    @Query("""
    SELECT new com.example.school.participants.assignedparticipants.ParticipantRequest(p.id, p.participantName)
    FROM AssignedParticipant ap
    JOIN Participants p ON ap.participantId = p.id
    WHERE ap.eventId = :eventId
""")
    Page<ParticipantRequest> findParticipantsByEvent(
            @Param("eventId") Long eventId,
            Pageable pageable
    );

    List<AssignedParticipant> findAllByEventIdAndCategory(Long eventId, String category);
}

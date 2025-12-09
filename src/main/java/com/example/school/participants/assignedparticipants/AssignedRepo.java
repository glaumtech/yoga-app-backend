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

    @Query("SELECT ap FROM AssignedParticipant ap " +
            "JOIN AssignedGroup ag ON ap.assignedGroupId = ag.id " +
            "WHERE ag.eventId = :eventId")
    List<AssignedParticipant> findAllByEventId(@Param("eventId") Long eventId);






@Query("SELECT ap FROM AssignedParticipant ap " +
        "JOIN AssignedGroup ag ON ap.assignedGroupId = ag.id " +
        "WHERE ag.eventId = :eventId " +
        "AND ap.category = :category")
List<AssignedParticipant> findAllByEventIdAndCategory(
        @Param("eventId") Long eventId,
        @Param("category") String category
);



    @Query(
            value = "SELECT ap.* " +
                    "FROM assigned_participant ap " +
                    "JOIN assigned_group ag ON ap.assigned_group_id = ag.id " +
                    "WHERE ag.event_id = :eventId",
            nativeQuery = true
    )
    List<AssignedParticipant> findByEventId(@Param("eventId") Long eventId);




    List<AssignedParticipant> findAllByAssignedGroupIdInAndIsScoredFalse(List<Long> groupIds);

    List<AssignedParticipant> findAllByAssignedGroupIdInAndJuryIdAndIsScoredFalse(List<Long> groupIds, Long juryId);

    List<AssignedParticipant> findAllByAssignedGroupIdAndJuryId(Long assignId, Long juryId);
}

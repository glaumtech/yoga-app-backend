package com.example.school.participants;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRep extends JpaRepository<Participants,Long> {

    Optional<Participants> findTopByOrderByIdDesc();

       @Query("""
    SELECT p FROM Participants p
    WHERE p.deleted = false
      AND (:status = 'Rejected' OR p.status <> 'Rejected')
      AND (:status IS NULL OR p.status = :status)
      AND (:participant IS NULL OR LOWER(p.participantName) LIKE LOWER(CONCAT('%', :participant, '%')) 
           OR LOWER(p.participantCode) LIKE LOWER(CONCAT('%', :participant, '%')))
      AND (:category IS NULL OR p.category = :category)
      AND (:groupName IS NULL OR LOWER(p.groupName) = LOWER(:groupName))
      AND p.eventId = :eventId
""")
    Page<Participants> findFilteredParticipants(
            @Param("participant") String participant,
            @Param("status") String status,
            @Param("category") String category,
            @Param("groupName") String groupName,
            @Param("eventId") Long eventId,
            Pageable pageable
    );

    boolean existsByParticipantNameIgnoreCaseAndDeletedFalse(String participantName);


    boolean existsByParticipantNameIgnoreCaseAndIdNotAndDeletedFalse(String participantName, Long id);
}

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
    List<Participants> findByStatus(String status);


    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected'")
    Page<Participants> findAllExcludingStatus(Pageable pageable);

    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected' AND LOWER(p.participantName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Participants> findByNameExcludingStatus(@Param("name") String name, Pageable pageable);

    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected' AND p.status = :status")
    Page<Participants> findByStatusExcludingRejected(@Param("status") String status, Pageable pageable);

    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected' AND LOWER(p.participantName) LIKE LOWER(CONCAT('%', :name, '%')) AND p.status = :status")
    Page<Participants> findByNameAndStatusExcludingRejected(@Param("name") String name, @Param("status") String status, Pageable pageable);

    Optional<Participants> findTopByOrderByIdDesc();

    /// /////////
//    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected'  AND p.eventId = :eventId")
//    Page<Participants> findAllExcludingStatusByEvent(@Param("eventId")Long eventId,Pageable pageable);
//    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected' AND p.status = :status AND p.eventId = :eventId")
//    Page<Participants> findByStatusExcludingRejectedByEvent(@Param("status") String status,@Param("eventId")Long eventId, Pageable pageable);
//
//    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected' AND  p.eventId = :eventId AND LOWER(p.participantName) LIKE LOWER(CONCAT('%', :name, '%'))")
//    Page<Participants> findByNameExcludingStatusByEvent(@Param("name") String name,@Param("eventId")Long eventId, Pageable pageable);
//
//
//    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected' AND LOWER(p.participantName) LIKE LOWER(CONCAT('%', :name, '%')) AND p.status = :status AND p.eventId = :eventId")
//    Page<Participants> findByNameAndStatusExcludingRejectedByEvent(@Param("name") String name, @Param("status") String status,@Param("eventId")Long eventId, Pageable pageable);
//    // All participants in an event with category
//    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected' AND p.eventId = :eventId AND p.category = :category")
//    Page<Participants> findAllExcludingStatusByEventAndCategory(@Param("eventId") Long eventId, @Param("category") String category, Pageable pageable);
//
//    // Filter by status + category
//    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected' AND p.status = :status AND p.eventId = :eventId AND p.category = :category")
//    Page<Participants> findByStatusExcludingRejectedByEventAndCategory(@Param("status") String status, @Param("eventId") Long eventId, @Param("category") String category, Pageable pageable);
//
//    // Filter by name + category
//    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected' AND LOWER(p.participantName) LIKE LOWER(CONCAT('%', :name, '%')) AND p.eventId = :eventId AND p.category = :category")
//    Page<Participants> findByNameExcludingStatusByEventAndCategory(@Param("name") String name, @Param("eventId") Long eventId, @Param("category") String category, Pageable pageable);
//
//    // Filter by name + status + category
//    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected' AND LOWER(p.participantName) LIKE LOWER(CONCAT('%', :name, '%')) AND p.status = :status AND p.eventId = :eventId AND p.category = :category")
//    Page<Participants> findByNameAndStatusExcludingRejectedByEventAndCategory(@Param("name") String name, @Param("status") String status, @Param("eventId") Long eventId, @Param("category") String category, Pageable pageable);
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

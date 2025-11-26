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
    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected'  AND p.eventId = :eventId")
    Page<Participants> findAllExcludingStatusByEvent(@Param("eventId")Long eventId,Pageable pageable);
    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected' AND p.status = :status AND p.eventId = :eventId")
    Page<Participants> findByStatusExcludingRejectedByEvent(@Param("status") String status,@Param("eventId")Long eventId, Pageable pageable);

    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected' AND  p.eventId = :eventId AND LOWER(p.participantName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Participants> findByNameExcludingStatusByEvent(@Param("name") String name,@Param("eventId")Long eventId, Pageable pageable);

    @Query("SELECT p FROM Participants p WHERE p.deleted = false AND p.status <> 'Rejected' AND LOWER(p.participantName) LIKE LOWER(CONCAT('%', :name, '%')) AND p.status = :status AND p.eventId = :eventId")
    Page<Participants> findByNameAndStatusExcludingRejectedByEvent(@Param("name") String name, @Param("status") String status,@Param("eventId")Long eventId, Pageable pageable);


    boolean existsByParticipantNameIgnoreCaseAndDeletedFalse(String participantName);


    boolean existsByParticipantNameIgnoreCaseAndIdNotAndDeletedFalse(String participantName, Long id);
}

package com.example.school.participants.assignedparticipants;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignedGroupRepository extends JpaRepository<AssignedGroup, Long> {
    Optional<AssignedGroup> findByEventIdAndTeamId(Long eventId, Long teamId);
    List<AssignedGroup> findAllByEventId(Long eventId);

    Page<AssignedGroup> findAllByEventId(Long eventId, Pageable pageable);
//    List<AssignedGroup> findAllByEventIdAndIsScoredFalse(Long eventId);
//
//    Page<AssignedGroup> findAllByEventIdAndIsScoredFalse(Long eventId, Pageable pageable);
}


package com.example.school.scoring;

import com.example.school.scoring.entity.Scoring;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public interface ScoringRepository extends JpaRepository<Scoring, Long> {


    List<Scoring> findByEventId(Long eventId);

    List<Scoring> findByEventIdAndParticipantIdAndDeletedFalse(Long eventId, Long participantId);
}


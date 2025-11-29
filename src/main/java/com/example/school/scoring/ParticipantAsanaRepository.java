package com.example.school.scoring;

import com.example.school.scoring.entity.ParticipantAsana;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParticipantAsanaRepository extends JpaRepository<ParticipantAsana,Long> {
    List<ParticipantAsana> findByScoringIdAndDeletedFalse(Long id);
}

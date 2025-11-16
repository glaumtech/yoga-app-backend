package com.example.school.participants;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParticipantRep extends JpaRepository<Participants,Long> {
    List<Participants> findByStatus(String status);
}

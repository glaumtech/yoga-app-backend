package com.example.school.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team,Long> {
    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);

    boolean existsByNameIgnoreCaseAndIdNotAndDeletedFalse(String name, Long id);

    List<Team> findAllByDeletedFalseAndEventId(Long eventId);
}

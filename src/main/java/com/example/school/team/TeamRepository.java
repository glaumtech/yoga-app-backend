package com.example.school.team;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team,Long> {

boolean existsByNameIgnoreCaseAndEventIdAndDeletedFalse(String name, Long eventId);

    boolean existsByNameIgnoreCaseAndIdNotAndDeletedFalse(String name, Long id);

   List<Team> findAllByDeletedFalseAndEventId(Long eventId);

    @Query("SELECT t FROM Team t JOIN t.juryList j WHERE j.id = :juryId AND t.eventId = :eventId AND t.deleted = false")
    List<Team> findTeamsByJuryAndEvent(Long juryId, Long eventId);

}

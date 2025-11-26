package com.example.school.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventRep extends JpaRepository<Event, Long> {
    List<Event> findByDeletedFalse();

    boolean existsByTitleIgnoreCaseAndStartDateAndEndDateAndDeletedFalse(
            String title, LocalDate startDate, LocalDate endDate
    );

    boolean existsByTitleIgnoreCaseAndIdNotAndDeletedFalse(String title, Long id);
}

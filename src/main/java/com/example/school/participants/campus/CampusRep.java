package com.example.school.participants.campus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampusRep extends JpaRepository<CampusList, Long> {
    List<CampusList> findByCategory(String category);
}

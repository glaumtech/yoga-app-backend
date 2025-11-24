package com.example.school.jury;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JuryRepository extends JpaRepository<Jury,Long> {
    List<Jury> findByDeletedFalseOrderByIdDesc();

}

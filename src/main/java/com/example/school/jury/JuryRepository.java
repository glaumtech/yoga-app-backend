package com.example.school.jury;

import com.example.school.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JuryRepository extends JpaRepository<Jury,Long> {
    List<Jury> findByDeletedFalseOrderByIdDesc();

    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);
    Optional<Jury> findByUserId(Long id);
    boolean existsByNameIgnoreCaseAndIdNotAndDeletedFalse(String name, Long id);
}

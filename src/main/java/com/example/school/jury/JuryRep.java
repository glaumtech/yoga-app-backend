package com.example.school.jury;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface JuryRep extends JpaRepository<Jury,Long> {

}

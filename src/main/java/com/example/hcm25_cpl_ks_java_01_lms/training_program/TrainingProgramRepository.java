package com.example.hcm25_cpl_ks_java_01_lms.training_program;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Long> {
    Page<TrainingProgram> findByProgramNameContainingIgnoreCase(String searchTerm, Pageable pageable);
    List<TrainingProgram> findByProgramNameContainingIgnoreCaseOrProgramCodeContainingIgnoreCase(String name, String code);


}

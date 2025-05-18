package com.example.hcm25_cpl_ks_java_01_lms.learningpath;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {
    Page<LearningPath> findByNameContainingIgnoreCase(String searchTerm, Pageable pageable);

    @Query("SELECT lp FROM LearningPath lp JOIN lp.users u WHERE u.id = :userId")
    Page<LearningPath> findByUser_Id(@Param("userId") Long userId, Pageable pageable);

    Page<LearningPath> findByUsers_IdAndNameContainingIgnoreCase(Long userId, String searchTerm, Pageable pageable);


}

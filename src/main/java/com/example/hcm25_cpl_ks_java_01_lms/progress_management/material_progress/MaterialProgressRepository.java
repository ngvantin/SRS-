package com.example.hcm25_cpl_ks_java_01_lms.progress_management.material_progress;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialProgressRepository extends JpaRepository<MaterialProgress, Long> {
    Optional<MaterialProgress> findByUserIdAndCourseIdAndSessionIdAndMaterialId(Long userId, Long courseId, Long sessionId, Long materialId);
    MaterialProgress findOneByUserIdAndCourseIdAndSessionIdAndMaterialId(Long userId, Long courseId, Long sessionId, Long materialId);
    List<MaterialProgress> findByUserIdAndCourseIdAndIsCompleted(Long userId, Long courseId, Boolean isCompleted);
    Page<MaterialProgress> findByUserIdAndCourseId(Long userId, Long courseId, Pageable pageable);
    Page<MaterialProgress> findByUserIdAndCourseIdAndMaterialId(Long userId, Long courseId, Long materialId, Pageable pageable);
    boolean existsByUserIdAndCourseIdAndMaterialIdAndIsCompletedTrue(Long userId, Long courseId, Long materialId);
    Optional<MaterialProgress> findTopByUserIdAndCourseIdOrderByLastAccessedAtDesc(Long userId, Long courseId);
}
package com.example.hcm25_cpl_ks_java_01_lms.progress_management;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressManagementRepository extends JpaRepository<ProgressManagement, Long> {
    List<ProgressManagement> findByUserIdAndCourseId(Long userId, Long courseId);
    Page<ProgressManagement> findByUserIdAndCourseId(Long userId, Long courseId, Pageable pageable);
    Page<ProgressManagement> findByUserId(Long userId, Pageable pageable);

}

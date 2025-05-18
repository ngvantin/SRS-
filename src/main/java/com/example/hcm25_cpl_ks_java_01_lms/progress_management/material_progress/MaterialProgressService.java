package com.example.hcm25_cpl_ks_java_01_lms.progress_management.material_progress;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaterialProgressService {

    private final MaterialProgressRepository materialProgressRepository;

    public void markMaterialAsCompleted(Long userId, Long courseId, Long sessionId, Long materialId) {
        MaterialProgress progress = materialProgressRepository
                .findByUserIdAndCourseIdAndSessionIdAndMaterialId(userId, courseId, sessionId, materialId)
                .orElse(MaterialProgress.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .sessionId(sessionId)
                        .materialId(materialId)
                        .isCompleted(false)
                        .build());

        progress.setIsCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        materialProgressRepository.save(progress);
    }

    public List<MaterialProgress> findCompletedMaterials(Long userId, Long courseId) {
        return materialProgressRepository.findByUserIdAndCourseIdAndIsCompleted(userId, courseId, true);
    }

    public boolean isMaterialCompleted(Long studentId, Long courseId, Long materialId) {
        return materialProgressRepository.existsByUserIdAndCourseIdAndMaterialIdAndIsCompletedTrue(studentId, courseId, materialId);
    }

    public MaterialProgress save(MaterialProgress progress) {
        return materialProgressRepository.save(progress);
    }

    public MaterialProgress findOneByUserIdAndCourseIdAndSessionIdAndMaterialId(Long userId, Long courseId, Long sessionId, Long materialId) {
        return materialProgressRepository.findOneByUserIdAndCourseIdAndSessionIdAndMaterialId(userId, courseId, sessionId, materialId);
    }
    public Optional<MaterialProgress> getLastAccessedMaterial(Long userId, Long courseId) {
        return materialProgressRepository
                .findTopByUserIdAndCourseIdOrderByLastAccessedAtDesc(userId, courseId);
    }

    public Page<MaterialProgress> findByUserAndCourse(Long userId, Long courseId, Pageable pageable) {
        return materialProgressRepository.findByUserIdAndCourseId(userId, courseId, pageable);
    }
}

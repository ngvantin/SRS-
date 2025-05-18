package com.example.hcm25_cpl_ks_java_01_lms.learningpath_course;

import com.example.hcm25_cpl_ks_java_01_lms.learningpath.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LearningPathCourseRepository extends JpaRepository<LearningPathCourse, LearningPathCourseId> {
    List<LearningPathCourse> findByLearningPathIdOrderByOrderNumberAsc(Long learningPathId);

    LearningPathCourse findByLearningPathIdAndCourseId(Long learningPathId, Long courseId);

    @Transactional
    void deleteByLearningPathIdAndCourseId(Long learningPathId, Long courseId);

    void deleteByLearningPathId(Long id);

    List<LearningPathCourse> findByLearningPath(LearningPath learningPath);
}

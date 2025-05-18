package com.example.hcm25_cpl_ks_java_01_lms.learningpath_course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathCourseId implements Serializable {
    private Long learningPathId;
    private Long courseId;
}

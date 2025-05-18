package com.example.hcm25_cpl_ks_java_01_lms.learningpath_course;

import com.example.hcm25_cpl_ks_java_01_lms.course.Course;
import com.example.hcm25_cpl_ks_java_01_lms.learningpath.LearningPath;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "learning_path_courses")
@IdClass(LearningPathCourseId.class) // Sử dụng IdClass cho khóa chính phức hợp
public class LearningPathCourse {

    @Id
    @Column(name = "learning_path_id")
    private Long learningPathId;

    @Id
    @Column(name = "course_id")
    private Long courseId;

    private Long orderNumber; // Trường số thứ tự, cho phép null

    @ManyToOne
    @JoinColumn(name = "learning_path_id", insertable = false, updatable = false)
    private LearningPath learningPath;

    @ManyToOne
    @JoinColumn(name = "course_id", insertable = false, updatable = false)
    private Course course;
}

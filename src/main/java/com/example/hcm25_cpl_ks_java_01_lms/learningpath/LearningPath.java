package com.example.hcm25_cpl_ks_java_01_lms.learningpath;


import com.example.hcm25_cpl_ks_java_01_lms.course.Course;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LearningPath {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Lob
    private String description;

    private String image;

    @ManyToMany
    @JoinTable(
            name = "learning_path_courses",
            joinColumns = @JoinColumn(name = "learning_path_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private List<Course> courses;

    @ManyToMany(mappedBy = "learningPaths")
    private List<User> users;

    @Transient // Không ánh xạ trường này vào database
    @Setter
    private List<LearningPathService.CourseWithOrder> coursesWithOrder;

    // Thêm getter cho coursesWithOrder nếu bạn chưa có
    public List<LearningPathService.CourseWithOrder> getCoursesWithOrder() {
        return coursesWithOrder;
    }

    // Thêm các thuộc tính khác nếu cần (ví dụ: người tạo, ngày tạo, ...)
}

package com.example.hcm25_cpl_ks_java_01_lms.topic;

import com.example.hcm25_cpl_ks_java_01_lms.course.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Integer> {
    Page<Topic> findAll(Pageable pageable);
    Page<Topic> findByTopicNameContainingIgnoreCase(String name, Pageable pageable);
    List<Topic> findByTopicNameContainingIgnoreCase(String searchTerm);
    boolean existsByTopicName(String name);
    List<Topic> findAll();
    Optional<Topic> findByTopicName(String name);
    Optional<Topic> findByTopicId(Integer topicId);

    @Query("SELECT t FROM Topic t LEFT JOIN FETCH t.tags WHERE t.course = :course")
    List<Topic> findByCourse(@Param("course") Course course);

    void deleteAllByTopicIdIn(List<Long> topicIds);

    boolean existsByTopicNameAndCourse(String trimmedTopicName, Course course);

    @Query("""
    SELECT t FROM Topic t
    WHERE (:keyword IS NULL 
           OR LOWER(t.topicName) LIKE LOWER(CONCAT('%', :keyword, '%')))
      OR (:courseId IS NULL OR t.course.id = :courseId)
""")
    Page<Topic> searchTopics(@Param("keyword") String keyword,
                             @Param("courseId") Long courseId,
                             Pageable pageable);


}

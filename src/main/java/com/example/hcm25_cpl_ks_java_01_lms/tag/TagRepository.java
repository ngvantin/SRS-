package com.example.hcm25_cpl_ks_java_01_lms.tag;

import com.example.hcm25_cpl_ks_java_01_lms.topic.Topic;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Page<Tag> findAll(Pageable pageable);
    Page<Tag> findByTagNameContainingIgnoreCase(String tagName, Pageable pageable);

    boolean existsByTagNameAndTopic(String trimmedTagName, Topic topic);

    List<Tag> findByTopic(Topic topic);

    void deleteByTagNameInAndTopic(List<String> tagsToDelete, Topic topic);
    Page<Tag> findByTopic(Topic topic, Pageable pageable); // Thêm phương thức này

    Page<Tag> findByTopic_TopicId(Integer topicId, Pageable pageable);
    boolean existsByTagNameAndTopic_TopicId(String tagName, Integer topicId);
}




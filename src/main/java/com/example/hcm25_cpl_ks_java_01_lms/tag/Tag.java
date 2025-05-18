package com.example.hcm25_cpl_ks_java_01_lms.tag;

import com.example.hcm25_cpl_ks_java_01_lms.topic.Topic;
import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;

    private String tagName;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

}

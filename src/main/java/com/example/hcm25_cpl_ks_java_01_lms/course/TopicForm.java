package com.example.hcm25_cpl_ks_java_01_lms.course;

import lombok.*;

import java.util.List;


@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TopicForm {
    private Integer topicId;
    private String topicName;
    private List<String> tags;
    private Integer index;
//    private String tags;

}

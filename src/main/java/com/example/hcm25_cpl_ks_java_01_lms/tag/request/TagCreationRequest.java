package com.example.hcm25_cpl_ks_java_01_lms.tag.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class TagCreationRequest {
    @NotNull(message = "Topic ID is required")
    private Integer topicId;
    @NotBlank(message = "Tag name cannot be blank")
    private List<String> tagNameList;
}

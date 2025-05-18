package com.example.hcm25_cpl_ks_java_01_lms.tag.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagUpdateRequest {
    @NotBlank(message = "Tag name is required")
    private String tagName;
}

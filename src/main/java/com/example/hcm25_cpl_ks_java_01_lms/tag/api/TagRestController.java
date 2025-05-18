package com.example.hcm25_cpl_ks_java_01_lms.tag.api;


import com.example.hcm25_cpl_ks_java_01_lms.tag.Tag;
import com.example.hcm25_cpl_ks_java_01_lms.tag.TagService;
import com.example.hcm25_cpl_ks_java_01_lms.tag.mapper.TagMapper;
import com.example.hcm25_cpl_ks_java_01_lms.tag.request.TagCreationRequest;
import com.example.hcm25_cpl_ks_java_01_lms.tag.request.TagUpdateRequest;
import com.example.hcm25_cpl_ks_java_01_lms.tag.response.TagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@PreAuthorize("@customSecurityService.hasRoleForModule(authentication, 'Tags - Rest')")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "API for managing tags")
public class TagRestController {

    private final TagService tagService;
    private final TagMapper tagMapper;

    @GetMapping("/topics/{topicId}")
    @Operation(summary = "Get all tags by Topic ID", description = "Retrieve a paginated list of tags for a specific topic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tags"),
            @ApiResponse(responseCode = "404", description = "Topic not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Page<TagResponse>> getAllTagsByTopicId(
            @PathVariable @Parameter(description = "ID of the topic") Integer topicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Tag> tagPage = tagService.getAllTagsByTopicId(topicId, pageable);
        return ResponseEntity.ok(tagPage.map(tagMapper::tagToTagResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID", description = "Retrieve details of a specific tag by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag found"),
            @ApiResponse(responseCode = "404", description = "Tag not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TagResponse> getTagById(@PathVariable @Parameter(description = "ID of the tag") Long id) {
        Tag tag = tagService.getTagById(id);

        return ResponseEntity.ok(tagMapper.tagToTagResponse(tag));
    }

    @PostMapping
    @Operation(summary = "Create multiple new tags for a topic", description = "Add multiple new tags to a specific topic")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tags created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or topic ID not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Tag>> createTags(@Valid @RequestBody @Parameter(description = "List of tag names and topic ID") TagCreationRequest request) {
        List<Tag> createdTags = tagService.createTagsApi(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTags);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing tag", description = "Update the name of a specific tag by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data or tag name already exists in this topic"),
            @ApiResponse(responseCode = "404", description = "Tag not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TagResponse> updateTag(@PathVariable @Parameter(description = "ID of the tag to update") Long id,
                                                 @Valid @RequestBody @Parameter(description = "Updated tag details") TagUpdateRequest updateRequest) {
        Tag updatedTag = tagService.updateTagApi(id, updateRequest);
        return ResponseEntity.ok(tagMapper.tagToTagResponse(updatedTag));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a tag", description = "Remove a tag from the system by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tag deleted successfully", content = @Content(schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "Tag not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(type = "string")))
    })
    public ResponseEntity<String> deleteTag(@PathVariable @Parameter(description = "ID of the tag to delete") Long id) {
        try {
            tagService.deleteTag(id);
            return ResponseEntity.ok("Tag deleted successfully");
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

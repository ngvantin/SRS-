package com.example.hcm25_cpl_ks_java_01_lms.tag.mapper;

import com.example.hcm25_cpl_ks_java_01_lms.tag.Tag;
import com.example.hcm25_cpl_ks_java_01_lms.tag.request.TagUpdateRequest;
import com.example.hcm25_cpl_ks_java_01_lms.tag.response.TagResponse;
import com.example.hcm25_cpl_ks_java_01_lms.topic.Topic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TagMapper {

    @Mapping(source = "topic.topicId", target = "topicId")
    TagResponse tagToTagResponse(Tag tag);

    List<TagResponse> tagsToTagResponses(List<Tag> tags);

    @Mapping(target = "tagId", ignore = true)
    @Mapping(source = "tagName", target = "tagName")
    @Mapping(source = "topic", target = "topic")
    Tag createTagFromTagNameAndTopic(String tagName, Topic topic);

    @Mapping(target = "tagId", ignore = true)
    @Mapping(source = "tagName", target = "tagName")
    void updateTagFromUpdateRequest(TagUpdateRequest updateRequest, @MappingTarget Tag tag);
}

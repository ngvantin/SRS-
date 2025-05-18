package com.example.hcm25_cpl_ks_java_01_lms.tag;

import com.example.hcm25_cpl_ks_java_01_lms.tag.mapper.TagMapper;
import com.example.hcm25_cpl_ks_java_01_lms.tag.request.TagCreationRequest;
import com.example.hcm25_cpl_ks_java_01_lms.tag.request.TagUpdateRequest;
import com.example.hcm25_cpl_ks_java_01_lms.topic.Topic;
import com.example.hcm25_cpl_ks_java_01_lms.topic.TopicRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TagService {

    private final TagRepository tagRepository;

    private final TopicRepository topicRepository;

    private final TagMapper tagMapper;

    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    public Tag getTagById(Long id) {
        Optional<Tag> optionalTag = tagRepository.findById(id);
        return optionalTag.orElse(null);
    }

    //    public Tag getTagById(Long id) {
//        return tagRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Tag not found with id: " + id));
//    }
    public void createTag(Tag tag) {
        tagRepository.save(tag);
    }

//    public void createTags(List<String> tagNames, Integer topicId) {
//        Topic topic = topicRepository.findById(topicId)
//                .orElseThrow(() -> new RuntimeException("Topic not found"));
//
//        List<Tag> tags = new ArrayList<>();
//        for (String tagName : tagNames) {
//            String trimmedTagName = tagName.trim();
//            if (tagRepository.existsByTagNameAndTopic(trimmedTagName, topic)) {
//                throw new RuntimeException("Tag name '" + trimmedTagName + "' already exists for this topic");
//            }
//            Tag tag = Tag.builder().tagName(trimmedTagName).topic(topic).build();
//            tags.add(tag);
//        }
//        tagRepository.saveAll(tags);
//    }

    public void createTags(List<String> tagNames, Integer topicId) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        List<String> errorMessages = new ArrayList<>(); // Danh sách lưu trữ thông báo lỗi
        for (String tagName : tagNames) {
            String trimmedTagName = tagName.trim();
            // Kiểm tra trùng lặp tagName trong cùng topic
            if (tagRepository.existsByTagNameAndTopic(trimmedTagName, topic)) {
                errorMessages.add("Tag name '" + trimmedTagName + "' already exists for this topic");
            } else {
                // Tạo đối tượng Tag
                Tag tag = Tag.builder().tagName(trimmedTagName).topic(topic).build();
                tagRepository.save(tag); // Lưu từng tag một
            }
        }

        if (!errorMessages.isEmpty()) {
            throw new RuntimeException(String.join("<br>", errorMessages)); // Ném ngoại lệ chứa các thông báo lỗi
        }
    }
    public void updateTag(Long id, Tag tag) {
        Tag existingTag = getTagById(id);
        if (existingTag != null) {
            existingTag.setTagName(tag.getTagName());
            existingTag.setTopic(tag.getTopic());
            tagRepository.save(existingTag);
        }
    }

    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }

    public Page<Tag> searchTags(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return tagRepository.findAll(pageable);
        } else {
            return tagRepository.findByTagNameContainingIgnoreCase(searchTerm, pageable);
        }
    }

    public ByteArrayInputStream generateExcel(List<Tag> tags) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Tags");

            // Tạo header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerCellStyle.setFont(font);

            String[] headers = {"Tag ID", "Tag Name", "Topic ID"};
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Tạo data rows
            int rowIdx = 1;
            for (Tag tag : tags) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(tag.getTagId());
                row.createCell(1).setCellValue(tag.getTagName());
                row.createCell(2).setCellValue(tag.getTopic().getTopicId());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public List<String> importTags(List<Tag> tags) {
        List<String> skippedTags = new ArrayList<>();
        for (Tag tag : tags) {
            if (tag.getTopic() == null) {
                skippedTags.add(tag.getTagName() + " (Topic not found)");
                continue;
            }
            if (tagRepository.existsByTagNameAndTopic(tag.getTagName(), tag.getTopic())) {
                skippedTags.add(tag.getTagName());
            } else {
                tagRepository.save(tag);
            }
        }
        return skippedTags;
    }

    public void saveAll(List<Tag> tags) {
        tagRepository.saveAll(tags);
    }

    public Page<Tag> findTagsByTopic(Topic topic, Pageable pageable) {
        return tagRepository.findByTopic(topic, pageable);
    }


    public Page<Tag> getAllTagsByTopicId(Integer topicId, Pageable pageable) {
        return tagRepository.findByTopic_TopicId(topicId, pageable);
    }



//    @Transactional
//    public List<Tag> createTagsApi(TagCreationRequest request) {
//        Topic topic = topicRepository.findById(request.getTopicId())
//                .orElseThrow(() -> new EntityNotFoundException("Topic not found with id: " + request.getTopicId()));
//
//        return request.getTagNameList().stream()
//                .filter(tagName -> !tagRepository.existsByTagNameAndTopic_TopicId(tagName, request.getTopicId()))
//                .map(tagName -> tagMapper.createTagFromTagNameAndTopic(tagName, topic))
//                .map(tagRepository::save)
//                .collect(Collectors.toList());
//    }

    @Transactional
    public List<Tag> createTagsApi(TagCreationRequest request) {
        Topic topic = topicRepository.findById(request.getTopicId())
                .orElseThrow(() -> new EntityNotFoundException("Topic not found with id: " + request.getTopicId()));

        return request.getTagNameList().stream()
                .filter(tagName -> !tagRepository.existsByTagNameAndTopic_TopicId(tagName, request.getTopicId()))
                .map(tagName -> tagMapper.createTagFromTagNameAndTopic(tagName, topic))
                .map(tagRepository::save)
                .collect(Collectors.toList());
    }
    @Transactional
    public Tag updateTagApi(Long id, TagUpdateRequest updateRequest) {
        Tag existingTag = getTagById(id);
        if (tagRepository.existsByTagNameAndTopic_TopicId(updateRequest.getTagName(), existingTag.getTopic().getTopicId())
                && !existingTag.getTagName().equalsIgnoreCase(updateRequest.getTagName())) {
            throw new IllegalArgumentException("Tag with name '" + updateRequest.getTagName() + "' already exists in this topic.");
        }
        tagMapper.updateTagFromUpdateRequest(updateRequest, existingTag);
        return tagRepository.save(existingTag);
    }


}
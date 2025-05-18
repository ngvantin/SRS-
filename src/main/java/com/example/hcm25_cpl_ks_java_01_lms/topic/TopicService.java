package com.example.hcm25_cpl_ks_java_01_lms.topic;

import com.example.hcm25_cpl_ks_java_01_lms.course.Course;
import com.example.hcm25_cpl_ks_java_01_lms.course.CourseRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TopicService {

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private CourseRepository courseRepository;

    public Page<Topic> findAllTopics(int page, int size) {
        Pageable pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.ASC, "topicId"));
        return topicRepository.findAll(pageable);
    }

    public Optional<Topic> findTopicByName(String name) { return topicRepository.findByTopicName(name);}

    public Page<Topic> findByNameContaining(String keyword, Pageable pageable) {
        return topicRepository.findByTopicNameContainingIgnoreCase(keyword, pageable);
    }

    public Page<Topic> searchTopics(String keyword, Long courseId, Pageable pageable) {
        if (keyword != null && keyword.isBlank()) {
            keyword = null; // bỏ keyword rỗng trắng
        }
        return topicRepository.searchTopics(keyword, courseId, pageable);
    }



    public Topic createTopic(Topic topic) {
        return topicRepository.save(topic);
    }

    public List<Topic> findAll() {return topicRepository.findAll(); }

    public Optional<Topic> getTopicById(Integer id) {
        return topicRepository.findById(id);
    }

    public void deleteTopic(Integer id) {
        Optional<Topic> topic = topicRepository.findById(id);
        if (topic.isPresent()) {
            topicRepository.save(topic.get());
            topicRepository.deleteById(id);
        }
    }

    public void deleteTopicsByTopicIds(List<Long> topicIds) {
        topicRepository.deleteAllByTopicIdIn(topicIds);
    }


    public void createTopics(List<String> topicNames, Long courseId) {
        System.out.println("Course : " + courseId);
        System.out.println("List topic Service: " + topicNames);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));


        List<String> errorMessages = new ArrayList<>(); // Danh sách lưu thông báo lỗi

        for (String topicName : topicNames) {
            String trimmedTopicName = topicName.trim();
            // Kiểm tra trùng lặp topic trong cùng course
            boolean check = topicRepository.existsByTopicNameAndCourse(trimmedTopicName, course);
            System.out.println("Kiem tra trung lap topic trong course: " + check);
            if (check) {
                errorMessages.add("Topic name '" + trimmedTopicName + "' already exists for this course.");
            } else {
                // Tạo đối tượng Topic
                Topic topic = Topic.builder().topicName(trimmedTopicName).course(course).build();
                topicRepository.save(topic); // Lưu từng topic một
            }
        }

        if (!errorMessages.isEmpty()) {
            throw new RuntimeException(String.join("<br>", errorMessages)); // Ném lỗi nếu có
        }
    }


    public ByteArrayInputStream exportToExcel(List<Topic> topics) throws IOException {
        return generateExcel(topics);
    }

    private ByteArrayInputStream generateExcel(List<Topic> topics) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Topics");

            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerCellStyle.setFont(font);

            String[] headers = {"ID", "Name"};
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Create data rows
            int rowIdx = 1;
            for (Topic topic : topics) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(topic.getTopicId());
                row.createCell(1).setCellValue(topic.getTopicName());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public void saveAllFromExcel(List<Topic> topics) {
        topicRepository.saveAll(topics);
    }

    public List<Topic> findByCourse(Course course) {
        return topicRepository.findByCourse(course);
    }

    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    public List<Topic> findTopicsByNameContaining(String searchTerm) {
        return topicRepository.findByTopicNameContainingIgnoreCase(searchTerm);
    }
}

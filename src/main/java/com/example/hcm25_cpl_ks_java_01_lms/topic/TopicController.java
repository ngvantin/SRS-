package com.example.hcm25_cpl_ks_java_01_lms.topic;

import com.example.hcm25_cpl_ks_java_01_lms.common.Constants;
import com.example.hcm25_cpl_ks_java_01_lms.course.Course;
import com.example.hcm25_cpl_ks_java_01_lms.course.CourseRepository;
import com.example.hcm25_cpl_ks_java_01_lms.course.CourseService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/topics")
@PreAuthorize("@customSecurityService.hasRoleForModule(authentication, 'Topic')")
public class TopicController {

    @Autowired
    private TopicService topicService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;


    @GetMapping
    public String getAllTopics(Model model,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Long courseId) {

        Page<Topic> topicsPage;
        System.out.println("Keyword and course Id: " + keyword + ", " + courseId);

        if ((keyword != null && !keyword.isBlank()) || courseId != null) {
            topicsPage = topicService.searchTopics(keyword, courseId, PageRequest.of(page, size));
        } else {
            topicsPage = topicService.findAllTopics(page, size);
        }

        List<Course> courses = courseService.getAllCourses();

        model.addAttribute("topics", topicsPage);
        model.addAttribute("courses", courses);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCourseId", courseId);
        model.addAttribute("content", "topics/list");

        return Constants.LAYOUT;
    }



    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("topic", new Topic());
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses); // Lấy danh sách khóa học
        model.addAttribute("content", "topics/createtopics");
        return Constants.LAYOUT;
    }

    @PostMapping
    public String createMultipleTopics(@RequestParam("courseId") Long courseId,
                                       @RequestParam("topicNames") List<String> topicNames,
                                       RedirectAttributes redirectAttributes) {
        try {
            System.out.println("Created topics list: " + topicNames);
            topicService.createTopics(topicNames, courseId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());  // Lưu lỗi vào redirectAttributes
            return "redirect:/topics/new";
        }
        return "redirect:/topics";
    }

    @GetMapping("/edit/{topicId}")
    public String showEditForm(@PathVariable Integer topicId, Model model) {
        Optional<Topic> topic = topicService.getTopicById(topicId);
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses); // Lấy danh sách khóa học
        if (topic.isPresent()) {
            model.addAttribute("topic", topic.get());
            model.addAttribute("content", "topics/edit");
            return Constants.LAYOUT;
        } else {
            return "redirect:/topics";
        }
    }

    @PostMapping("/edit/{topicId}")
    public String updateTopic(@PathVariable Integer topicId, @ModelAttribute Topic topic) {
        topic.setTopicId(topicId);
        topicService.createTopic(topic);
        return "redirect:/topics";
    }

    @GetMapping("/delete/{topicId}")
    public String deleteTopic(@PathVariable Integer topicId) {
        topicService.deleteTopic(topicId);
        return "redirect:/topics";
    }

    @PostMapping("/deleteSelected")
    @Transactional
    public String deleteSelectedTopics(@RequestParam("selectedTopics") List<Long> topicIds,
                                       RedirectAttributes redirectAttributes) {

        if (topicIds != null && !topicIds.isEmpty()) {
            topicService.deleteTopicsByTopicIds(topicIds);
            redirectAttributes.addFlashAttribute("success", "Selected topics deleted successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "No topics selected for deletion.");
        }
        return "redirect:/topics";
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportToExcel(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Topic> topics = topicService.findAll();

            ByteArrayInputStream in = topicService.exportToExcel(topics);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=topics.xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(new InputStreamResource(in));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/print")
    public String printTopics(Model model) {
        List<Topic> topics = topicService.findAll(); // Lấy danh sách topics từ service
        model.addAttribute("topics", topics);
        return "topics/print"; // Trả về file print-topics.html
    }

    @GetMapping("/download-template")
    public ResponseEntity<Resource> downloadTopicExcelTemplate() {
        try {
            // Đường dẫn tới file template của topics
            Path filePath = Paths.get("data-excel/topic_template.xlsx");
            Resource resource = new ByteArrayResource(Files.readAllBytes(filePath));

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=topic_template.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('Admin')")
    public String importExcel(@RequestParam("file") MultipartFile file, Model model) {
        model.addAttribute("content", "topics/list");
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select a file to upload");
            return Constants.LAYOUT;
        }

        try {
            List<Topic> topics = TopicExcelImporter.importTopics(file.getInputStream(), courseRepository);

            // Lấy danh sách topic_name đã có trong database từ findAll()
            Set<String> existingTopicNames = topicService.findAll()
                    .stream()
                    .map(Topic::getTopicName)
                    .collect(Collectors.toSet());

            // Lọc danh sách chỉ giữ những topic chưa tồn tại
            List<Topic> newTopics = topics.stream()
                    .filter(topic -> !existingTopicNames.contains(topic.getTopicName()))
                    .collect(Collectors.toList());

            if (newTopics.isEmpty()) {
                model.addAttribute("error", "All topics in the file already exist in the database");
            } else {
                topicService.saveAllFromExcel(newTopics);
                model.addAttribute("success", "Successfully uploaded and imported " + newTopics.size() + " new topics");
            }

            return "redirect:/topics";
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to import data from file");
            return Constants.LAYOUT;
        }
    }

}

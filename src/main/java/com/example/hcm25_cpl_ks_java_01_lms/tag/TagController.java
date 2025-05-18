package com.example.hcm25_cpl_ks_java_01_lms.tag;

import com.example.hcm25_cpl_ks_java_01_lms.common.Constants;
import com.example.hcm25_cpl_ks_java_01_lms.topic.Topic;
import com.example.hcm25_cpl_ks_java_01_lms.topic.TopicRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/tags")
@PreAuthorize("@customSecurityService.hasRoleForModule(authentication, 'Tag')")
public class TagController {

    @Autowired
    private TagService tagService;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private TagRepository tagRepository;
//
//    @GetMapping
//    public String listTags(Model model,
//                           @RequestParam(defaultValue = "0") int page,
//                           @RequestParam(defaultValue = "10") int size,
//                           @RequestParam(value = "searchTerm", required = false) String searchTerm) {
//        try {
//            Pageable pageable = PageRequest.of(page, size);
//            Page<Tag> tagPage = tagService.searchTags(searchTerm, pageable); // Cập nhật service
//            List<Topic> allTopics = topicRepository.findAll();
//            model.addAttribute("tagPage", tagPage);
//            model.addAttribute("searchTerm", searchTerm);
//            model.addAttribute("allTopics", allTopics);
//            model.addAttribute("selectedTopicId", null); // Đảm bảo không có topic nào được chọn mặc định
//            model.addAttribute("content", "tags/list");
//            return Constants.LAYOUT;
//        }catch (IllegalArgumentException e) {
//            model.addAttribute("error", e.getMessage());
//            return Constants.LAYOUT;
//        }
//
//    }
//
//    @GetMapping("/{topicId}")
//    public String listTagsByTopic(Model model,
//                                  @RequestParam(defaultValue = "0") int page,
//                                  @RequestParam(defaultValue = "10") int size,
//                                  @RequestParam(value = "searchTerm", required = false) String searchTerm,
//                                  @RequestParam("topicId") Integer topicId) {
//        try {
//            Pageable pageable = PageRequest.of(page, size);
//            Topic topic = topicRepository.findById(topicId).orElse(null);
//            Page<Tag> tagPage;
//
//            if (topic != null) {
//                tagPage = tagService.findTagsByTopic(topic, pageable);
//                model.addAttribute("selectedTopicId", topicId);
//            } else {
//                // Xử lý trường hợp topic không tồn tại (có thể redirect hoặc hiển thị thông báo lỗi)
//                return "redirect:/tags"; // Hoặc hiển thị trang list tags với thông báo lỗi
//            }
//
//            List<Topic> allTopics = topicRepository.findAll();
//            model.addAttribute("tagPage", tagPage);
//            model.addAttribute("searchTerm", searchTerm);
//            model.addAttribute("allTopics", allTopics);
//            model.addAttribute("content", "tags/list");
//            return Constants.LAYOUT;
//        } catch (IllegalArgumentException e) {
//            model.addAttribute("error", e.getMessage());
//            return Constants.LAYOUT;
//        }
//    }

    @GetMapping
    public String listTags(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(value = "searchTerm", required = false) String searchTerm,
                           @RequestParam(value = "topicId", required = false) Integer topicId) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Tag> tagPage;

            if (topicId != null && topicId > 0) {
                // Lấy các tag theo topicId
                Topic topic = topicRepository.findById(topicId).orElse(null);
                if (topic != null) {
                    tagPage = tagService.findTagsByTopic(topic, pageable); // Cần thêm phương thức này vào TagService
                    model.addAttribute("selectedTopicId", topicId); // Để giữ trạng thái selected trong combobox
                } else {
                    tagPage = tagService.searchTags(searchTerm, pageable); // Hiển thị tất cả nếu topic không tồn tại
                    model.addAttribute("selectedTopicId", null);
                }
            } else {
                // Lấy tất cả các tag hoặc tìm kiếm theo searchTerm
                tagPage = tagService.searchTags(searchTerm, pageable);
                model.addAttribute("selectedTopicId", null);
            }

            List<Topic> allTopics = topicRepository.findAll();
            model.addAttribute("tagPage", tagPage);
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("allTopics", allTopics); // Thêm danh sách topic vào model
            model.addAttribute("content", "tags/list");
            return Constants.LAYOUT;
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return Constants.LAYOUT;
        }
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("tag", new Tag());
        List<Topic> topics = topicRepository.findAll();
        model.addAttribute("topics", topics);
        model.addAttribute("content", "tags/create");
        return Constants.LAYOUT;
    }

    @PostMapping("/create")
    public String createTag(@ModelAttribute Tag tag) {
        tagService.createTag(tag);
        return "redirect:/tags";
    }

    @GetMapping("/new")
    public String showCreateTagsForm(Model model) {
        model.addAttribute("tag", new Tag());
        List<Topic> topics = topicRepository.findAll();
        model.addAttribute("topics", topics);
        model.addAttribute("content", "tags/createtags");
        return Constants.LAYOUT;
    }

    @PostMapping("/create-multiple")
    public String createMultipleTags(@RequestParam("topicId") Integer topicId,
                                     @RequestParam("tagNames") String tagNames,
                                     RedirectAttributes redirectAttributes) {
        try {
            List<String> tagList = Arrays.asList(tagNames.split(",")); // Tách chuỗi thành List
            tagService.createTags(tagList, topicId);
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/tags/new";
        }
        return "redirect:/tags";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Tag tag = tagService.getTagById(id);
        model.addAttribute("tag", tag);
        List<Topic> topics = topicRepository.findAll();
        model.addAttribute("topics", topics);
        model.addAttribute("content", "tags/edit");
        return Constants.LAYOUT;
    }

    @PostMapping("/edit/{id}")
    public String updateTag(@PathVariable Long id, @ModelAttribute Tag tag) {
        tagService.updateTag(id, tag);
        return "redirect:/tags";
    }

    @PostMapping("/delete/{id}")
    public String deleteTag(@PathVariable Long id, Model model) {
        try {
            tagService.deleteTag(id);
            return "redirect:/tags";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/tags";
        }
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportToExcel(
            @RequestParam(value = "searchTerm", required = false) String searchTerm) throws IOException {
        List<Tag> tags = tagService.searchTags(searchTerm, Pageable.unpaged()).getContent();

        ByteArrayInputStream in = tagService.generateExcel(tags); // Gọi service để tạo Excel
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=tags.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(new InputStreamResource(in));
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please upload a file.");
            return "redirect:/tags";
        }

        try {
            List<Tag> tags = TagExcelImporter.importTags(file.getInputStream(), topicRepository, tagRepository);
            List<String> skippedTags = tagService.importTags(tags); // Sử dụng service để xử lý logic
            if (!skippedTags.isEmpty()) {
                redirectAttributes.addFlashAttribute("warning", "Skipped tags: " + String.join(", ", skippedTags));
            }
            redirectAttributes.addFlashAttribute("message", "File uploaded and data imported successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to import data from the file.");
        }
        return "redirect:/tags";
    }

    @PostMapping("/delete-all")
    @Transactional
    public ResponseEntity<String> deleteSelectedTags(@RequestBody DeleteRequest deleteRequest, Model model) {
        try {
            List<Long> ids = deleteRequest.getIds();
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body("No tags selected for deletion");
            }
            for (Long id : ids) {
                tagService.deleteTag(id);
            }
            return ResponseEntity.ok("Tags deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete tags: " + e.getMessage());
        }
    }

    // Class để nhận dữ liệu từ request body
    public static class DeleteRequest {
        private List<Long> ids;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }
    }
}

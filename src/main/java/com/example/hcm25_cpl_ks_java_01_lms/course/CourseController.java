package com.example.hcm25_cpl_ks_java_01_lms.course;

import com.example.hcm25_cpl_ks_java_01_lms.common.Constants;
import com.example.hcm25_cpl_ks_java_01_lms.topic.Topic;
import com.example.hcm25_cpl_ks_java_01_lms.topic.TopicService;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserRepository;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/courses")
@PreAuthorize("@customSecurityService.hasRoleForModule(authentication, 'Course')")
public class CourseController{
    private final CourseService courseService;
    private final UserService userService;

    private final TopicService topicService;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public CourseController(UserRepository userRepository, CourseRepository courseRepository, CourseService courseService, UserService userService, TopicService topicService) {
        this.courseService = courseService;
        this.userService = userService;
        this.topicService = topicService;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
    }

    @GetMapping
    public String listCourses(Model model,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String searchTerm) {
        Page<Course> courses = courseService.getAllCourses(searchTerm, page, size);
        model.addAttribute("courses", courses);
        if (courses == null || courses.getContent() == null) {
            model.addAttribute("courses", new PageImpl<>(new ArrayList<>()));  // Tránh trường hợp null
        }
        model.addAttribute("content", "courses/list");
        return Constants.LAYOUT;
    }

    @GetMapping("/search-prerequisites")
    @ResponseBody
    public List<Course> searchPrerequisites(@RequestParam String keyword) {
        return courseService.getAllCourses(keyword);
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("course", new Course());

        // Truyền danh sách user vào model
        List<User> users = userService.getAllUsers("", 0, 100).getContent();
        model.addAttribute("users", users);

        // Truyền danh sách tất cả các khóa học vào model (cho Prerequisites)
        List<Course> allCourses = courseService.getAllCourses();
        model.addAttribute("allCourses", allCourses);

        model.addAttribute("content", "courses/create");
        return Constants.LAYOUT;
    }

    @PostMapping
    public String createCourse(@ModelAttribute @Valid Course course,
                               BindingResult result,
                               @RequestParam("imageFile") MultipartFile file,
                               @RequestParam(value = "prerequisites", required = false) List<Long> prerequisiteIds,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("course", course);
            model.addAttribute("users", userService.getAllUsers("", 0, 100).getContent());
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("content", "courses/create");
            return Constants.LAYOUT;
        }

        try {
            List<Course> prerequisites = prerequisiteIds != null
                    ? prerequisiteIds.stream().map(courseService::getCourseById).collect(Collectors.toList())
                    : null;

            course.setPrerequisites(prerequisites);
            courseService.createCourse(course, file);

            return "redirect:/courses";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage()); // <-- Thêm lỗi này để hiển thị trên giao diện
            model.addAttribute("course", course);
            model.addAttribute("users", userService.getAllUsers("", 0, 100).getContent());
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("content", "courses/create");
            return Constants.LAYOUT;
        }
    }


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Course course = courseService.getCourseById(id);
        if (course != null) {
            model.addAttribute("course", course);

            // Truyền danh sách users và khóa học vào model
            model.addAttribute("users", userService.getAllUsers("", 0, 100).getContent());
            model.addAttribute("allCourses", courseService.getAllCourses());

            model.addAttribute("content", "courses/update");
            return Constants.LAYOUT;
        }
        return "redirect:/courses";
    }


    @PostMapping("/edit/{id}")
    public String updateCourse(@PathVariable Long id,
                               @ModelAttribute @Valid Course courseDetails,
                               BindingResult result,
                               @RequestParam(value = "imageFile", required = false) MultipartFile file,
                               @RequestParam(value = "prerequisites", required = false) List<Long> prerequisiteIds,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("course", courseDetails);
            model.addAttribute("users", userService.getAllUsers("", 0, 100).getContent());
            model.addAttribute("allCourses", courseService.getAllCourses());
            model.addAttribute("content", "courses/update");
            return Constants.LAYOUT;
        }

        if (file != null && !file.isEmpty()) {
            String fileType = file.getContentType();
            if (!fileType.startsWith("image/")) {
                throw new IllegalArgumentException("Uploaded file must be an image");
            }
        }

        try {
            // Lấy khóa học gốc từ ID để cập nhật dữ liệu đúng
            Course existingCourse = courseService.getCourseById(id);
            if (existingCourse == null) {
                throw new RuntimeException("Course not found with ID: " + id);
            }

            // Cập nhật thông tin từ courseDetails
            existingCourse.setName(courseDetails.getName());
            existingCourse.setCode(courseDetails.getCode());
            existingCourse.setDescription(courseDetails.getDescription());
            existingCourse.setPrice(courseDetails.getPrice());
            existingCourse.setDiscount(courseDetails.getDiscount());
            existingCourse.setDurationInWeeks(courseDetails.getDurationInWeeks());
            existingCourse.setLanguage(courseDetails.getLanguage());
            existingCourse.setLevel(courseDetails.getLevel());
            existingCourse.setPublished(courseDetails.isPublished());

            // Xử lý creator và instructor
            existingCourse.setCreator(courseDetails.getCreator());
            existingCourse.setInstructor(courseDetails.getInstructor());

            // Cập nhật danh sách prerequisites
            List<Course> prerequisites = (prerequisiteIds != null && !prerequisiteIds.isEmpty())
                    ? prerequisiteIds.stream()
                    .map(courseService::getCourseById)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
                    : new ArrayList<>();

            existingCourse.setPrerequisites(prerequisites);

            // Cập nhật hình ảnh (nếu có)
            courseService.updateCourse(id, existingCourse, file);

            return "redirect:/courses";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update course: " + e.getMessage());
            model.addAttribute("course", courseDetails);

            // Truyền lại danh sách users và khóa học nếu có lỗi
            model.addAttribute("users", userService.getAllUsers("", 0, 100).getContent());
            model.addAttribute("allCourses", courseService.getAllCourses());

            model.addAttribute("content", "courses/update");

            return Constants.LAYOUT;
        }
    }


    @GetMapping("/edit/session")
    public String showSessionPage(@RequestParam("courseId") Long courseId, Model model) {
        Course course = courseService.getCourseById(courseId);
        if (course != null) {
            model.addAttribute("course", course);
            model.addAttribute("content", "courses/editsession");
            return Constants.LAYOUT;
        }
        return "redirect:/courses";
    }
//
//    @GetMapping("/edit/topic-tags")
//    public String editTopicTags(@RequestParam("courseId") Long courseId, Model model) {
//        Course course = courseService.getCourseById(courseId);
//
//        if (course != null) {
//            List<Topic> courseTopics = topicService.findByCourse(course);
//            List<Topic> allTopics = topicService.getAllTopics(); // Lấy tất cả các topic hiện có
//
//            // Tạo và khởi tạo TopicFormListWrapper
//            TopicFormListWrapper wrapper = new TopicFormListWrapper();
//            List<TopicForm> topicForms = courseTopics.stream()
//                    .map(topic -> TopicForm.builder()
//                            .topicId(topic.getTopicId()) // Sử dụng topicId để chọn
//                            .topicName(topic.getTopicName())
//                            .tags(topic.getTags().stream().map(Tag::getTagName).collect(Collectors.toList()))
//                            .index(topic.getTopicId()) // Thiết lập index từ topicId
//                            .build())
//                    .collect(Collectors.toList());
//            wrapper.setTopics(topicForms);
//
//            model.addAttribute("course", course);
//            model.addAttribute("courseTopics", courseTopics); // Danh sách các topic hiện tại của khóa học
//            model.addAttribute("allTopics", allTopics);       // Danh sách tất cả các topic có sẵn
//            model.addAttribute("topicFormListWrapper", wrapper); // Đảm bảo wrapper được thêm vào model
//            model.addAttribute("content", "courses/topictags"); // Đưa vào layout chung
//            return Constants.LAYOUT;
//        }
//        return "redirect:/courses"; // Điều hướng nếu không tìm thấy course
//    }


    @GetMapping("/edit/topic-tags")
    public String editTopicTags(@RequestParam("courseId") Long courseId, Model model,
                                @RequestParam(value = "searchTerm", required = false) String searchTerm) {
        Course course = courseService.getCourseById(courseId);

        if (course != null) {
            List<Topic> allTopics;
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                allTopics = topicService.findTopicsByNameContaining(searchTerm); // Giả sử có method này
            } else {
                allTopics = topicService.getAllTopics();
            }

            model.addAttribute("course", course); // Thêm thông tin course để hiển thị tên
            model.addAttribute("allTopics", allTopics); // Danh sách tất cả các topic có sẵn (hoặc đã lọc)
            model.addAttribute("searchTerm", searchTerm); // Giữ lại searchTerm để hiển thị trên form
            model.addAttribute("content", "courses/topictags"); // Đưa vào layout chung
            return Constants.LAYOUT;
        }
        return "redirect:/courses"; // Điều hướng nếu không tìm thấy course
    }

    @PostMapping("/add-topic")
    public ResponseEntity<String> addTopicToCourse(@RequestParam("courseId") Long courseId,
                                                   @RequestParam("topicId") Integer topicId) {
        Course course = courseService.getCourseById(courseId);
        topicService.getTopicById(topicId).ifPresent(topic -> {
            courseService.addTopicToCourse(course, topic);
        });
        // Sau khi thêm thành công, có thể bạn muốn trả về thông tin cập nhật của course
        return ResponseEntity.status(HttpStatus.OK).body("Topic added and linked to course");
    }

    @PostMapping("/remove-topic")
    public ResponseEntity<String> removeTopicFromCourse(@RequestParam("courseId") Long courseId,
                                                        @RequestParam("topicId") Integer topicId) {
        Course course = courseService.getCourseById(courseId);
        topicService.getTopicById(topicId).ifPresent(topic -> {
            courseService.removeTopicFromCourse(course, topic);
        });
        return ResponseEntity.status(HttpStatus.OK).body("Topic removed successfully");
    }

    @PostMapping("/courses/save-topics-tags")
    public String saveTopicsTags(@RequestParam("id") Long courseId,
                                 @RequestParam(value = "addedTopicIds", required = false) List<Integer> addedTopicIds,
                                 Model model) {
        Course course = courseService.getCourseById(courseId);
        if (course != null && addedTopicIds != null && !addedTopicIds.isEmpty()) {
            final Course finalCourse = course;
            for (Integer topicId : addedTopicIds) {
                topicService.getTopicById(topicId).ifPresent(topic -> {
                    topic.setCourse(finalCourse);
                    if (finalCourse.getTopics() == null) {
                        finalCourse.setTopics(new ArrayList<>());
                    }
                    if (!finalCourse.getTopics().contains(topic)) {
                        finalCourse.getTopics().add(topic);
                    }
                });
            }
            courseService.saveCourse(course);
            course = courseService.getCourseByIdWithTopicsAndTags(courseId);
        }
        model.addAttribute("course", course);
        model.addAttribute("content", "courses/topictags");
        return Constants.LAYOUT;
    }

//    @PostMapping("/add-topic")
//    public ResponseEntity<String> addTopicToCourse(@RequestParam("courseId") Long courseId,
//                                                   @RequestParam("topicId") Integer topicId) {
//        Course course = courseService.getCourseById(courseId);
//        Topic topic = topicService.getTopicById(topicId).get();
//
//        if (course != null && topic != null) {
//            courseService.addTopicToCourse(course, topic);
//            return ResponseEntity.ok("Topic added successfully");
//        } else {
//            return ResponseEntity.badRequest().body("Course or Topic not found");
//        }
//    }


    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<Map<String, Object>> toggleCourseStatus(@PathVariable Long id) {
        Course updatedCourse = courseService.toggleCourseStatus(id);
        Map<String, Object> response = new HashMap<>();
        response.put("id", updatedCourse.getId());
        response.put("published", updatedCourse.isPublished());
        return ResponseEntity.ok(response);
    }

    @ModelAttribute("topicFormListWrapper")
    public TopicFormListWrapper getTopicFormListWrapper() {
        TopicFormListWrapper wrapper = new TopicFormListWrapper();
        wrapper.setTopics(new ArrayList<>());
        return wrapper;
    }

    @PostMapping("/save-topics-tags")
    public String saveTopicsAndTags(@RequestParam("id") Long courseId,
                                    @ModelAttribute("topicFormListWrapper") TopicFormListWrapper wrapper,
                                    RedirectAttributes redirectAttributes) {
        List<TopicForm> topicForms = wrapper.getTopics();

        // Log chi tiết để kiểm tra dữ liệu
        log.info("Received topicForms (wrapper): {}", topicForms);

        if (topicForms != null && !topicForms.isEmpty()) {
            log.info("Number of topics received: {}", topicForms.size());

            for (int i = 0; i < topicForms.size(); i++) {
                TopicForm topicForm = topicForms.get(i);
                log.info("Topic #{}: {}", i + 1, topicForm.getTopicName());

                if (topicForm.getTags() != null && !topicForm.getTags().isEmpty()) {
                    log.info("Tags for Topic #{}: {}", i + 1, String.join(", ", topicForm.getTags()));
                } else {
                    log.info("No tags found for Topic #{}", i + 1);
                }
            }
        } else {
            log.info("No topics received.");
        }

        try {
            // Gọi service để lưu dữ liệu
            courseService.saveCourseTopicsAndTags(courseId, topicForms);
            redirectAttributes.addFlashAttribute("message", "Topics and tags saved successfully.");
        } catch (Exception e) {
            log.error("Failed to save topics and tags for course ID {}: {}", courseId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Failed to save topics and tags: " + e.getMessage());
        }

        return "redirect:/courses/edit/topic-tags?courseId=" + courseId;
    }

    @PostMapping("/delete/{id}")
    public String deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return "redirect:/courses"; // Chuyển hướng về danh sách khóa học sau khi xóa
    }

    @PostMapping("/delete-selected")
    public ResponseEntity<?> deleteSelectedCourses(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.badRequest().body("No course IDs provided");
        }

        for (Long id : ids) {
            try {
                courseService.deleteCourse(id); // gọi service đã xử lý đầy đủ
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to delete course with ID " + id + ": " + e.getMessage());
            }
        }

        return ResponseEntity.ok().body("Courses deleted successfully");
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        try {
            List<Course> courses = courseService.getAllCourses();

            // Xuất file Excel vào bộ nhớ
            ByteArrayInputStream in = courseService.exportToExcel(courses);
            if (in == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }

            // Cấu hình response headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=courses.xlsx");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('Admin')")
    public String importExcel(@RequestParam("file") MultipartFile file, Model model) {
        model.addAttribute("content", "courses/list");

        // Kiểm tra nếu file không được chọn
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select a file to upload");
            return Constants.LAYOUT;
        }

        try {
            // Dùng CourseExcelImporter để nhập khóa học từ file Excel
            CourseExcelImporter importer = new CourseExcelImporter(userRepository, courseRepository);
            List<Course> courses = importer.importCourses(file.getInputStream());
            courseService.saveAllFromExcel(courses);

            // Thêm thông báo thành công vào model
            model.addAttribute("success", "Successfully uploaded and imported data");

            // Chuyển hướng về trang danh sách khóa học
            return "redirect:/courses";
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to import data from file");
            return Constants.LAYOUT;
        } catch (RuntimeException e) {
            // Xử lý lỗi nếu không tìm thấy người dùng hoặc khóa học tiền đề
            model.addAttribute("error", e.getMessage());
            return Constants.LAYOUT;
        }
    }



//    @PostMapping("/save-topics-tags")
//    public String saveTopicsAndTags(HttpServletRequest request, @RequestParam("id") Long courseId,
//                                    @ModelAttribute("topicForms") List<TopicForm> topicForms,
//                                    RedirectAttributes redirectAttributes) {
//        log.info("ở đây ----------------1");
//        try {
//            log.info("ở đây ----------------2");
//            log.info("All request parameters: {}", request.getParameterMap());
//            log.info("topicForms: {}", topicForms); // Sử dụng định dạng để in đối tượng
//            log.info("courseId: {}", courseId); // Sử dụng định dạng để in đối tượng
//            courseService.saveCourseTopicsAndTags(courseId, topicForms);
//            redirectAttributes.addFlashAttribute("message", "Topics and tags saved successfully.");
//        } catch (Exception e) {
//            log.info("ở đây ----------------3");
//            e.printStackTrace();
//            redirectAttributes.addFlashAttribute("error", "Failed to save topics and tags: " + e.getMessage());
//        }
//        log.info("ở đây ----------------4");
//        return "redirect:/courses/edit/topic-tags?courseId=" + courseId;
//    }


//    @PostMapping("/save-topics-tags")
//    public String saveTopicsAndTags(@RequestParam("id") Long courseId,
//                                    @RequestParam(value = "topics[0].topicName", required = false) String topic0Name,
//                                    @RequestParam(value = "topics[0].tags", required = false) String topic0Tags,
//                                    // ... (thêm các @RequestParam cho các index khác nếu bạn biết số lượng tối đa)
//                                    RedirectAttributes redirectAttributes) {
//        log.info("Received topic0Name: {}", topic0Name);
//        log.info("Received topic0Tags: {}", topic0Tags);
//        // ... (tự tạo TopicForm và xử lý)
//        return "redirect:/courses/edit/topic-tags?courseId=" + courseId;
//    }

//    // Inner class to represent the form data for a Topic and its Tags
//    public static class TopicForm {
//        private String topicName;
//        private List<String> tags;
//
//        public String getTopicName() {
//            return topicName;
//        }
//
//        public void setTopicName(String topicName) {
//            this.topicName = topicName;
//        }
//
//        public List<String> getTags() {
//            return tags;
//        }
//
//        public void setTags(List<String> tags) {
//            this.tags = tags;
//        }
//    }
}
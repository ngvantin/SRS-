package com.example.hcm25_cpl_ks_java_01_lms.learningpath;


import com.example.hcm25_cpl_ks_java_01_lms.common.Constants;
import com.example.hcm25_cpl_ks_java_01_lms.course.Course;
import com.example.hcm25_cpl_ks_java_01_lms.course.CourseRepository;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/learning-paths")
@PreAuthorize("@customSecurityService.hasRoleForModule(authentication, 'Learning Path')")
@RequiredArgsConstructor
public class LearningPathController{

    private final LearningPathService learningPathService;

    private final CourseRepository courseRepository;

    private final LearningPathExcelImporter learningPathExcelImporter;

    @GetMapping
    public String listLearningPaths(Model model,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(value = "searchTerm", required = false) String searchTerm) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LearningPath> learningPathPage = learningPathService.searchLearningPaths(searchTerm, pageable); // Cập nhật service

        // Lấy danh sách các khóa học đã sắp xếp kèm theo số thứ tự cho mỗi Learning Path
        Map<Long, List<LearningPathService.CourseWithOrder>> learningPathCoursesWithOrder = new HashMap<>();
        for (LearningPath learningPath : learningPathPage.getContent()) {
            LearningPath fullLearningPath = learningPathService.getLearningPathWithOrderedCourses(learningPath.getId());
            learningPathCoursesWithOrder.put(fullLearningPath.getId(), fullLearningPath.getCoursesWithOrder());
        }

        model.addAttribute("learningPathPage", learningPathPage);
        model.addAttribute("searchTerm", searchTerm); // Thêm searchTerm vào model
        model.addAttribute("content", "learningpaths/list");
        model.addAttribute("learningPathCoursesWithOrder", learningPathCoursesWithOrder); // Thêm map vào model


        return Constants.LAYOUT;
    }

    @GetMapping("/student")
    public String getStudentLearningPaths(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            User student = (User) authentication.getPrincipal();
            Long studentId = student.getId();

            // Lấy danh sách Learning Paths mà sinh viên đã đăng ký
            List<LearningPath> myLearningPaths = learningPathService.getEnrolledLearningPaths(studentId);

            // Lấy tất cả Learning Paths
            List<LearningPath> allLearningPaths = learningPathService.getAllLearningPaths();

            // Lọc ra những Learning Paths mà sinh viên chưa đăng ký
            List<LearningPath> discoverLearningPaths = allLearningPaths.stream()
                    .filter(lp -> !myLearningPaths.contains(lp))
                    .collect(Collectors.toList());

            model.addAttribute("myLearningPaths", myLearningPaths);
            model.addAttribute("discoverLearningPaths", discoverLearningPaths);
            model.addAttribute("content", "student_learnings/learning_paths"); // Tên fragment
            return Constants.LAYOUT;
        } else {
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/view/{id}")
    public String viewLearningPath(@PathVariable Long id, Model model, Authentication authentication) {
        LearningPath learningPath = learningPathService.getLearningPathById(id);
        if (learningPath != null) {
            model.addAttribute("learningPath", learningPath);
            boolean isEnrolled = false;
            if (authentication != null && authentication.isAuthenticated()) {
                User user = (User) authentication.getPrincipal();
                isEnrolled = learningPathService.isUserEnrolled(user.getId(), id); // Cần implement method này trong service
            }
            model.addAttribute("isEnrolled", isEnrolled);
            model.addAttribute("content", "learningpaths/detail");
            return Constants.LAYOUT;
        } else {
            return "redirect:/learning-paths";
        }
    }

    @GetMapping("/my-learning-paths")
    public String getMyLearningPaths(Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(value = "searchTerm", required = false) String searchTerm) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();

            Pageable pageable = PageRequest.of(page, size);
            Page<LearningPath> myLearningPathPage = learningPathService.getLearningPathsByUser(userId,searchTerm ,pageable);

            // Lấy danh sách các khóa học đã sắp xếp kèm theo số thứ tự cho mỗi Learning Path
            Map<Long, List<LearningPathService.CourseWithOrder>> learningPathCoursesWithOrder = new HashMap<>();
            for (LearningPath learningPath : myLearningPathPage.getContent()) {
                LearningPath fullLearningPath = learningPathService.getLearningPathWithOrderedCourses(learningPath.getId());
                learningPathCoursesWithOrder.put(fullLearningPath.getId(), fullLearningPath.getCoursesWithOrder());
            }

            model.addAttribute("myLearningPathPage", myLearningPathPage); // Sử dụng myLearningPathPage
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("content", "learningpaths/my-learning-paths");
            model.addAttribute("learningPathCoursesWithOrder", learningPathCoursesWithOrder); // Thêm map vào model


            return Constants.LAYOUT;
        } else {
            return "redirect:/auth/login";
        }
    }


    @GetMapping("/enroll/{id}")
    public String enrollLearningPath(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            try {
                learningPathService.enrollUserInLearningPath(user.getId(), id);
            } catch (RuntimeException e) {
                // Thêm thông báo lỗi vào redirectAttributes
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
            return "redirect:/learning-paths";
        } else {
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/my-learning-paths/delete/{id}")
    public String unenrollLearningPath(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            try {
                learningPathService.unenrollUserFromLearningPath(user.getId(), id);
            } catch (RuntimeException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
            }
            return "redirect:/learning-paths/my-learning-paths";
        } else {
            return "redirect:/auth/login";
        }
    }
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("learningPath", new LearningPath());
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
//        return "learningpaths/create";
        model.addAttribute("content", "learningpaths/create");
        return Constants.LAYOUT;
    }

//    @PostMapping("/create")
//    public String createLearningPath(@ModelAttribute LearningPath learningPath, @RequestParam("selectedCourses") List<Long> selectedCourseIds,@RequestParam(value = "courseOrder", required = false) List<Long> courseOrders) {
////        learningPathService.createLearningPath(learningPath, selectedCourseIds);
//        learningPathService.createLearningPathWithOrder(learningPath, selectedCourseIds, courseOrders);
//
//        return "redirect:/learning-paths";
//    }

    @PostMapping("/create")
    public String createLearningPath(@ModelAttribute LearningPath learningPath,
                                     @RequestParam("selectedCourses") List<Long> selectedCourseIds,
                                     @RequestParam("courseOrder") String courseOrderJson) {
        try {
            List<Long> courseOrder = new ObjectMapper().readValue(courseOrderJson, new TypeReference<List<Long>>() {});
            learningPathService.createLearningPathWithOrder(learningPath, selectedCourseIds, courseOrder);
        } catch (JsonProcessingException e) {
            // Xử lý lỗi JSON nếu cần
            e.printStackTrace();
            // Có thể redirect về form tạo với thông báo lỗi
            return "redirect:/learning-paths/new?error=Invalid course order";
        }
        return "redirect:/learning-paths";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
//        LearningPath learningPath = learningPathService.getLearningPathById(id);
        LearningPath learningPath = learningPathService.getLearningPathByIdWithOrder(id);
        model.addAttribute("learningPath", learningPath);
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        model.addAttribute("content", "learningpaths/edit");
        return Constants.LAYOUT;
    }

    @PostMapping("/edit/{id}")
    public String updateLearningPath(@PathVariable Long id, @ModelAttribute LearningPath learningPath, @RequestParam("selectedCourses") List<Long> selectedCourseIds,  @RequestParam("courseOrder") String courseOrderJson) {
//        learningPathService.updateLearningPath(id, learningPath, selectedCourseIds);
//        return "redirect:/learning-paths";
        try {
            List<Long> courseOrder = new ObjectMapper().readValue(courseOrderJson, new TypeReference<List<Long>>() {});
            learningPathService.updateLearningPathWithOrder(id, learningPath, selectedCourseIds, courseOrder);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "redirect:/learning-paths/edit/" + id + "?error=Invalid course order";
        }
        return "redirect:/learning-paths";
    }

    @PostMapping("/delete/{id}")
    public String deleteLearningPath(@PathVariable Long id, Model model) {
        try {
            learningPathService.deleteLearningPath(id);
            return "redirect:/learning-paths";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/learning-paths";
        }
    }

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportToExcel(
            @RequestParam(value = "searchTerm", required = false) String searchTerm) throws IOException {
        List<LearningPath> learningPaths = learningPathService.searchLearningPaths(searchTerm, Pageable.unpaged()).getContent();

        ByteArrayInputStream in = learningPathService.generateExcel(learningPaths);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=learning_paths.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(new InputStreamResource(in));
    }
//
//    @PostMapping("/import")
//    public String importExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
//        if (file.isEmpty()) {
//            redirectAttributes.addFlashAttribute("error", "Please upload a file.");
//            return "redirect:/learning-paths";
//        }
//
//        try {
//            List<LearningPath> learningPaths = LearningPathExcelImporter.importLearningPaths(file.getInputStream(), courseRepository);
//            learningPathService.saveAll(learningPaths);
//            redirectAttributes.addFlashAttribute("message", "File uploaded and data imported successfully.");
//        } catch (IOException e) {
//            e.printStackTrace();
//            redirectAttributes.addFlashAttribute("error", "Failed to import data from the file.");
//        }
//        return "redirect:/learning-paths";
//    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please upload a file.");
            return "redirect:/learning-paths";
        }

        try {
            List<LearningPathExcelImporter.LearningPathWithOrder> learningPathsWithOrder = learningPathExcelImporter.importLearningPathsWithOrder(file.getInputStream());
            learningPathService.importLearningPathsWithOrder(learningPathsWithOrder);
            redirectAttributes.addFlashAttribute("message", "File uploaded and data imported successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to import data from the file.");
        }
        return "redirect:/learning-paths";
    }

    @PostMapping("/delete-all")
    @Transactional
    public ResponseEntity<String> deleteSelectedLearningPaths(@RequestBody DeleteRequest deleteRequest, Model model) {
        try {
            List<Long> ids = deleteRequest.getIds();
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body("No learning paths selected for deletion");
            }
            for (Long id : ids) {
                learningPathService.deleteLearningPath(id);
            }
            return ResponseEntity.ok("Learning paths deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete learning paths: " + e.getMessage());
        }
    }

    @PostMapping("/my-learning-paths/delete-all")
    @Transactional
    public ResponseEntity<String> unenrollSelectedLearningPaths(@RequestBody DeleteRequest deleteRequest, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();
            try {
                List<Long> ids = deleteRequest.getIds();
                if (ids == null || ids.isEmpty()) {
                    return ResponseEntity.badRequest().body("No learning paths selected for unenrolling");
                }
                for (Long learningPathId : ids) {
                    learningPathService.unenrollUserFromLearningPath(userId, learningPathId);
                }
                return ResponseEntity.ok("Successfully unenrolled from selected learning paths");
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to unenroll from learning paths: " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
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
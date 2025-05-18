package com.example.hcm25_cpl_ks_java_01_lms.progress_management;

import com.example.hcm25_cpl_ks_java_01_lms.common.Constants;
import com.example.hcm25_cpl_ks_java_01_lms.progress_management.material_progress.MaterialProgress;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/progress")
@PreAuthorize("@customSecurityService.hasRoleForModule(authentication, 'Progress')")
public class ProgressManagementController {

    private final ProgressManagementService progressManagementService;
    private final UserService userService;

    public ProgressManagementController(ProgressManagementService progressManagementService, UserService userService) {
        this.progressManagementService = progressManagementService;
        this.userService = userService;
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public String getAllUsersProgress(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            Model model) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            userPage = progressManagementService.searchUserProgress(searchTerm, pageable);
        } else {
            userPage = progressManagementService.getAllUsersProgress(pageable);
        }

        Map<Long, Long> totalCoursesMap = new HashMap<>();
        Map<Long, Long> completedCoursesMap = new HashMap<>();
        for (User user : userPage.getContent()) {
            Page<ProgressManagement> progressPage = progressManagementService.getAllUserProgress(user.getId(), Pageable.unpaged());
            long totalCourses = progressPage.getTotalElements();
            long completedCourses = progressPage.getContent().stream()
                    .filter(course -> "completed".equalsIgnoreCase(course.getStatus()))
                    .count();
            totalCoursesMap.put(user.getId(), totalCourses);
            completedCoursesMap.put(user.getId(), completedCourses);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long myUserId = user.getId();
        User myUser = userService.getUserById(myUserId);

        Page<ProgressManagement> myUserCoursesPage = progressManagementService.getAllUserProgress(myUserId, pageable);

        long totalCourses = myUserCoursesPage.getTotalElements();
        long completedCourses = myUserCoursesPage.getContent().stream()
                .filter(course -> "completed".equalsIgnoreCase(course.getStatus()))
                .count();

        model.addAttribute("users", userPage.getContent());
        model.addAttribute("totalCoursesMap", totalCoursesMap);
        model.addAttribute("completedCoursesMap", completedCoursesMap);
        model.addAttribute("currentPage", userPage.getNumber());
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("searchTerm", searchTerm);

        model.addAttribute("user", myUser);
        model.addAttribute("userCourses", myUserCoursesPage.getContent());
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("completedCourses", completedCourses);
        model.addAttribute("content", "management_progress/list");
        return Constants.LAYOUT;
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public String getUserCourses(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size,
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            Model model) {
        User user = userService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProgressManagement> userCoursesPage;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            userCoursesPage = progressManagementService.searchUserCourses(userId, searchTerm, pageable);
        } else {
            userCoursesPage = progressManagementService.getAllUserProgress(userId, pageable);
        }

        long totalCourses = userCoursesPage.getTotalElements();
        long completedCourses = userCoursesPage.getContent().stream()
                .filter(course -> "completed".equalsIgnoreCase(course.getStatus()))
                .count();

        // Tính completedMaterials và totalMaterials cho từng course
        Map<Long, Long> totalMaterialsMap = new HashMap<>();
        Map<Long, Long> completedMaterialsMap = new HashMap<>();
        for (ProgressManagement course : userCoursesPage.getContent()) {
            Page<MaterialProgress> materialsPage = progressManagementService.getMaterialsByUserAndCourse(userId, course.getCourseId(), Pageable.unpaged());
            long totalMaterials = materialsPage.getTotalElements();
            long completedMaterials = materialsPage.getContent().stream()
                    .filter(MaterialProgress::getIsCompleted)
                    .count();
            totalMaterialsMap.put(course.getCourseId(), totalMaterials);
            completedMaterialsMap.put(course.getCourseId(), completedMaterials);
        }

        model.addAttribute("user", user);
        model.addAttribute("userCourses", userCoursesPage.getContent());
        model.addAttribute("totalMaterialsMap", totalMaterialsMap);
        model.addAttribute("completedMaterialsMap", completedMaterialsMap);
        model.addAttribute("currentPage", userCoursesPage.getNumber());
        model.addAttribute("totalPages", userCoursesPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("completedCourses", completedCourses);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("content", "management_progress/detail");
        return Constants.LAYOUT;
    }

    @GetMapping("/my-progress")
    @PreAuthorize("isAuthenticated()")
    public String getMyProgress(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        Long myUserId = user.getId();
        User myUser = userService.getUserById(myUserId);

        Pageable pageable = PageRequest.of(page, size);
        Page<ProgressManagement> myUserCoursesPage = progressManagementService.getAllUserProgress(myUserId, pageable);

        long totalCourses = myUserCoursesPage.getTotalElements();
        long completedCourses = myUserCoursesPage.getContent().stream()
                .filter(course -> "completed".equalsIgnoreCase(course.getStatus()))
                .count();

        // Tính completedMaterials và totalMaterials cho từng course
        Map<Long, Long> totalMaterialsMap = new HashMap<>();
        Map<Long, Long> completedMaterialsMap = new HashMap<>();
        for (ProgressManagement course : myUserCoursesPage.getContent()) {
            Page<MaterialProgress> materialsPage = progressManagementService.getMaterialsByUserAndCourse(myUserId, course.getCourseId(), Pageable.unpaged());
            long totalMaterials = materialsPage.getTotalElements();
            long completedMaterials = materialsPage.getContent().stream()
                    .filter(MaterialProgress::getIsCompleted)
                    .count();
            totalMaterialsMap.put(course.getCourseId(), totalMaterials);
            completedMaterialsMap.put(course.getCourseId(), completedMaterials);
        }

        model.addAttribute("user", myUser);
        model.addAttribute("userCourses", myUserCoursesPage.getContent());
        model.addAttribute("totalMaterialsMap", totalMaterialsMap);
        model.addAttribute("completedMaterialsMap", completedMaterialsMap);
        model.addAttribute("currentPage", myUserCoursesPage.getNumber());
        model.addAttribute("totalPages", myUserCoursesPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("totalCourses", totalCourses);
        model.addAttribute("completedCourses", completedCourses);
        model.addAttribute("content", "management_progress/list");
        return Constants.LAYOUT;
    }

    @GetMapping("/{userId}/course/{courseId}/materials")
    @PreAuthorize("isAuthenticated()")
    public String getCourseMaterials(
            @PathVariable Long userId,
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            Model model) {
        User user = userService.getUserById(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<MaterialProgress> materialsPage;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            materialsPage = progressManagementService.searchMaterialsByUserAndCourse(userId, courseId, searchTerm, pageable);
        } else {
            materialsPage = progressManagementService.getMaterialsByUserAndCourse(userId, courseId, pageable);
        }

        model.addAttribute("user", user);
        model.addAttribute("courseId", courseId);
        model.addAttribute("materials", materialsPage.getContent());
        model.addAttribute("currentPage", materialsPage.getNumber());
        model.addAttribute("totalPages", materialsPage.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("totalMaterials", materialsPage.getTotalElements());
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("content", "management_progress/material_list");
        return Constants.LAYOUT;
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportUserProgress() {
        try {
            ByteArrayResource resource = progressManagementService.exportUserProgressToExcel();
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_progress.xlsx")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{userId}/export")
    public ResponseEntity<Resource> exportUserCourseProgress(@PathVariable Long userId) {
        try {
            ByteArrayResource resource = progressManagementService.exportUserCourseProgressToExcel(userId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=course_progress.xlsx")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{userId}/course/{courseId}/materials/export")
    public ResponseEntity<Resource> exportMaterialProgress(@PathVariable Long userId, @PathVariable Long courseId) {
        try {
            ByteArrayResource resource = progressManagementService.exportMaterialProgressToExcel(userId, courseId);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=material_progress.xlsx")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
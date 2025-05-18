package com.example.hcm25_cpl_ks_java_01_lms.student_learning;

import com.example.hcm25_cpl_ks_java_01_lms.classes.ClassesService;
import com.example.hcm25_cpl_ks_java_01_lms.config.security.JwtTokenUtils;
import com.example.hcm25_cpl_ks_java_01_lms.course.Course;
import com.example.hcm25_cpl_ks_java_01_lms.course.CourseService;
import com.example.hcm25_cpl_ks_java_01_lms.course_enrollment.CourseEnrollment;
import com.example.hcm25_cpl_ks_java_01_lms.course_enrollment.CourseEnrollmentService;
import com.example.hcm25_cpl_ks_java_01_lms.material.CourseMaterial;
import com.example.hcm25_cpl_ks_java_01_lms.material.CourseMaterialService;
import com.example.hcm25_cpl_ks_java_01_lms.progress_management.material_progress.MaterialProgress;
import com.example.hcm25_cpl_ks_java_01_lms.progress_management.material_progress.MaterialProgressService;
import com.example.hcm25_cpl_ks_java_01_lms.session.Session;
import com.example.hcm25_cpl_ks_java_01_lms.session.SessionService;
import com.example.hcm25_cpl_ks_java_01_lms.student_learning.dto.EnrolledCourseDTO;
import com.example.hcm25_cpl_ks_java_01_lms.student_learning.dto.SessionDTO;
import com.example.hcm25_cpl_ks_java_01_lms.training_program_enrollment.TrainingProgramEnrollmentService;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
//@PreAuthorize("@customSecurityService.hasRoleForModule(authentication, 'Student Courses')")
public class StudentLearningController {

    private final UserService userService;
    private final CourseService courseService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private CourseMaterialService courseMaterialService;

    @Autowired
    private TrainingProgramEnrollmentService trainingProgramEnrollmentService;

    @Autowired
    private ClassesService classesService;

    @Autowired
    private MaterialProgressService materialProgressService;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @Autowired
    private StudentLearningService studentLearningService;

    @Autowired
    private CourseEnrollmentService courseEnrollmentService;

    @Autowired
    public StudentLearningController(UserService userService, CourseService courseService) {
        this.userService = userService;
        this.courseService = courseService;
    }

    @GetMapping("/courses")
    public String getStudentCourses(
            @RequestParam(value = "search", required = false) String keyword,
            HttpServletRequest request,
            Model model, String tab) {
        // 👉 Lấy studentId từ JWT và gán vào biến toàn cục
        String token = jwtTokenUtils.extractTokenFromRequest(request);
        Long studentId = jwtTokenUtils.extractUserId(token);

        User student = userService.getUserById(studentId);

        List<EnrolledCourseDTO> enrolledCoursesByLeaningPath = userService.getEnrolledCourses(studentId);
//        List<EnrolledCourseDTO> enrolledCoursesByTrainingProgram = trainingProgramEnrollmentService.getStudentCourses(studentId);
        List<EnrolledCourseDTO> enrolledCoursesByClass = classesService.getStudentCoursesFromClasses(studentId);
        List<CourseEnrollment> enrolledSingleCourses = courseEnrollmentService.getEnrollmentsByUser(studentId);

//        List<EnrolledCourseDTO> enrolledCourses = Stream.of(enrolledCoursesByLeaningPath, enrolledCoursesByTrainingProgram, enrolledCoursesByClass)
//                .flatMap(List::stream)
//                .collect(Collectors.toMap(EnrolledCourseDTO::getId, course -> course, (existing, replacement) -> existing))
//                .values()
//                .stream()
//                .toList();

        List<EnrolledCourseDTO> enrolledCourses = new ArrayList<>();
        enrolledCourses.addAll(enrolledCoursesByLeaningPath);
//        enrolledCourses.addAll(enrolledCoursesByTrainingProgram);
        enrolledCourses.addAll(enrolledCoursesByClass);
        for (CourseEnrollment enrollment : enrolledSingleCourses) {
            Course course = enrollment.getCourse();
            EnrolledCourseDTO dto = EnrolledCourseDTO.builder()
                    .id(course.getId())
                    .name(course.getName())
                    .description(course.getDescription())
                    .code(course.getCode())
                    .image(course.getImage())
                    .lastAccessed(enrollment.getEnrollmentDate()) // hoặc course.getLastAccessed() nếu có
                    .build();

            enrolledCourses.add(dto);
        }

        // Recent course
        List<Course> recentCourses = studentLearningService.getRecentlyAccessedCourses(studentId);

        // get all course
        List<Course> courses = courseService.getAllCourses();

        // Nếu có từ khóa, lọc theo tên hoặc mô tả
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.trim().toLowerCase();
            courses = courses.stream()
                    .filter(course -> course.getName().toLowerCase().contains(searchKeyword)
                            || course.getDescription().toLowerCase().contains(searchKeyword))
                    .collect(Collectors.toList());

            enrolledCourses = enrolledCourses.stream()
                    .filter(course -> course.getName().toLowerCase().contains(keyword)
                            || (course.getDescription() != null && course.getDescription().toLowerCase().contains(keyword)))
                    .collect(Collectors.toList());
        }

        // Lấy danh sách các courseId mà student đã enroll
        Set<Long> enrolledCourseIds = enrolledCourses.stream()
                .map(EnrolledCourseDTO::getId)
                .collect(Collectors.toSet());

        model.addAttribute("activeTab", tab);
        model.addAttribute("recentCourses", recentCourses);
        model.addAttribute("enrolledCourses", enrolledCourses);
        model.addAttribute("studentId", studentId);
        model.addAttribute("user", student);
        model.addAttribute("courses", courses);
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        model.addAttribute("content", "student_learnings/courses");
        return "LAYOUT";
    }

    @GetMapping("/courses/{courseId}/learn")
    public String learnCourse(
//            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long materialId,
            HttpServletRequest request,
            Model model) {

        String token = jwtTokenUtils.extractTokenFromRequest(request);
        Long studentId = jwtTokenUtils.extractUserId(token);
        Pageable pageable = PageRequest.of(page, size);

        Course course = courseService.getCourseById(courseId);
        List<Session> sessions = course.getSessions()
                .stream()
                .sorted(Comparator.comparing(Session::getOrderNumber))
                .collect(Collectors.toList());
        ;
        Session currentSession = null;
        List<CourseMaterial> courseMaterials = null;
        Long nextSessionId = null;
        CourseMaterial currentMaterial = null;

        if (sessionId != null) {
            currentSession = sessionService.getSessionById(sessionId);
            Page<CourseMaterial> materials = courseMaterialService.getMaterialsBySessionId(sessionId, search, page, size);
            courseMaterials = materials.getContent();
            int currentSessionIndex = sessions.indexOf(currentSession);

            if (currentSessionIndex < sessions.size() - 1) {
                nextSessionId = sessions.get(currentSessionIndex + 1).getId();
            }
        } else if (!sessions.isEmpty()) {
//            currentSession = sessions.get(0);
//            sessionId = currentSession.getId();
//            Page<CourseMaterial> materials = courseMaterialService.getMaterialsBySessionId(currentSession.getId(), search, page, size);
//            courseMaterials = materials.getContent();
//            if (sessions.size() > 1) {
//                nextSessionId = sessions.get(1).getId();
//            }
            // Tìm material gần nhất mà user đã truy cập trong course
            Optional<MaterialProgress> lastAccessed = materialProgressService.getLastAccessedMaterial(studentId, courseId);

            if (lastAccessed.isPresent()) {
                MaterialProgress progress = lastAccessed.get();
                sessionId = progress.getSessionId();
                materialId = progress.getMaterialId();
                currentSession = sessionService.getSessionById(sessionId);
            } else {
                // Không có dữ liệu truy cập, mặc định là session đầu tiên
                currentSession = sessions.get(0);
                sessionId = currentSession.getId();
            }

            Page<CourseMaterial> materials = courseMaterialService.getMaterialsBySessionId(sessionId, search, page, size);
            courseMaterials = materials.getContent();

            int currentSessionIndex = sessions.indexOf(currentSession);
            if (currentSessionIndex < sessions.size() - 1) {
                nextSessionId = sessions.get(currentSessionIndex + 1).getId();
            }
        }

        // Lấy currentMaterial theo materialId nếu có
        if (courseMaterials != null && !courseMaterials.isEmpty()) {
            if (materialId != null) {
                for (CourseMaterial material : courseMaterials) {
                    if (material.getId().equals(materialId)) {
                        currentMaterial = material;
                        break;
                    }
                }
            }

            // Nếu không có materialId hoặc không tìm thấy, lấy phần tử đầu tiên
            if (currentMaterial == null) {
                currentMaterial = courseMaterials.get(0);
            }
        }

        // Lấy danh sách MaterialProgress đã hoàn thành
        List<MaterialProgress> materialProgressList = materialProgressService.findCompletedMaterials(studentId, courseId);

        // Kiểm tra material hiện tại đã hoàn thành chưa
        boolean materialCompleted = false;
        if (currentMaterial != null) {
            CourseMaterial finalCurrentMaterial = currentMaterial;
            materialCompleted = materialProgressList.stream()
                    .anyMatch(mp -> mp.getMaterialId().equals(finalCurrentMaterial.getId()));
        }

        // Đánh dấu những material đã hoàn thành (dùng để hiển thị giao diện)
        Map<Long, Boolean> materialCompletedMap = new HashMap<>();
        for (MaterialProgress mp : materialProgressList) {
            materialCompletedMap.put(mp.getMaterialId(), true);
        }

        studentLearningService.updateLastAccessed(studentId, courseId);

        // Progress for material
        if (currentMaterial != null && sessionId != null) {
//            System.out.println("studentId: " + studentId);
//            System.out.println("courseId: " + courseId);
//            System.out.println("sessionId: " + sessionId);
//            System.out.println("materialId: " + currentMaterial.getId());

            MaterialProgress progress = materialProgressService
                    .findOneByUserIdAndCourseIdAndSessionIdAndMaterialId(studentId, courseId, sessionId, currentMaterial.getId());

            if (progress == null) {
                progress = new MaterialProgress();
                progress.setUserId(studentId);
                progress.setCourseId(courseId);
                progress.setSessionId(sessionId);
                progress.setMaterialId(currentMaterial.getId());
                progress.setAccessCount(0); // khởi tạo mặc định
                progress.setTotalStudyTime(Duration.ZERO); // hoặc null nếu muốn
            }

            // Update access info
            progress.setAccessCount(progress.getAccessCount() == null ? 1 : progress.getAccessCount() + 1);
            progress.setLastAccessedAt(LocalDateTime.now());

            // Cộng dồn thời gian học ước tính mỗi lần truy cập
            Duration learningIncrement = Duration.ofMinutes(1); // hoặc truyền từ phía client
            if (progress.getTotalStudyTime() == null) {
                progress.setTotalStudyTime(learningIncrement);
            } else {
                progress.setTotalStudyTime(progress.getTotalStudyTime().plus(learningIncrement));
            }

            materialProgressService.save(progress);
        }

        //         Số lượng material đã hoàn thành
        long completedCount = materialProgressList.stream()
                .filter(MaterialProgress::getIsCompleted)
                .count();
        Page<MaterialProgress> totalMaterials = materialProgressService.findByUserAndCourse(studentId, courseId, pageable);
        long totalCount = totalMaterials.getTotalElements();

        model.addAttribute("studentId", studentId);
        model.addAttribute("course", course);
        model.addAttribute("courseSessions", sessions);
        model.addAttribute("currentSession", currentSession);
        model.addAttribute("currentMaterial", currentMaterial);
        model.addAttribute("materials", courseMaterials);
        model.addAttribute("nextSessionId", nextSessionId);
        model.addAttribute("materialCompleted", materialCompleted);
        model.addAttribute("materialCompletedMap", materialCompletedMap);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("content", "student_learnings/learn_session");

        return "LAYOUT";
    }


    @PostMapping("/courses/{courseId}/learn/progress/update")
    public String updateProgress(
//            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @RequestParam Long sessionId,
            @RequestParam Long materialId,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        String token = jwtTokenUtils.extractTokenFromRequest(request);
        Long studentId = jwtTokenUtils.extractUserId(token);

        // Cập nhật tiến trình học
        materialProgressService.markMaterialAsCompleted(studentId, courseId, sessionId, materialId);

        // Thêm thông báo để hiển thị trên UI
        redirectAttributes.addFlashAttribute("successMessage", "Material marked as completed!");

        // Chuyển hướng về trang học tập hiện tại
        return "redirect:/student/courses/{courseId}/learn?sessionId=" + sessionId;
    }

    @GetMapping("/courses/{courseId}/learn/material/{materialId}/status")
    public Boolean checkMaterialStatus(
//            @PathVariable Long studentId,
            @PathVariable Long courseId,
            @PathVariable Long materialId,
            HttpServletRequest request) {

        String token = jwtTokenUtils.extractTokenFromRequest(request);
        Long studentId = jwtTokenUtils.extractUserId(token);
        return materialProgressService.isMaterialCompleted(studentId, courseId, materialId);
    }

    @GetMapping("/courses/{id}")
    public String getCourseDetail(@PathVariable Long id, Model model, HttpServletRequest request) {
        Course course = courseService.findById(id);

        course.setSessions(
                course.getSessions().stream()
                        .sorted(Comparator.comparing(Session::getOrderNumber))
                        .collect(Collectors.toList())
        );

        // Fetch materials for each session and sort them using CourseMaterialService
        for (Session session : course.getSessions()) {
            List<CourseMaterial> sortedMaterials = courseMaterialService.findBySessionId(session.getId());
        }

        String token = jwtTokenUtils.extractTokenFromRequest(request);
        Long studentId = jwtTokenUtils.extractUserId(token);

        List<EnrolledCourseDTO> enrolledCoursesByLeaningPath = userService.getEnrolledCourses(studentId);
//        List<EnrolledCourseDTO> enrolledCoursesByTrainingProgram = trainingProgramEnrollmentService.getStudentCourses(studentId);
        List<EnrolledCourseDTO> enrolledCoursesByClass = classesService.getStudentCoursesFromClasses(studentId);
        List<CourseEnrollment> enrolledSingleCourses = courseEnrollmentService.getEnrollmentsByUser(studentId);


        List<EnrolledCourseDTO> enrolledCourses = new ArrayList<>();
        enrolledCourses.addAll(enrolledCoursesByLeaningPath);
//        enrolledCourses.addAll(enrolledCoursesByTrainingProgram);
        enrolledCourses.addAll(enrolledCoursesByClass);
        for (CourseEnrollment enrollment : enrolledSingleCourses) {
            Course courseEnr = enrollment.getCourse();
            EnrolledCourseDTO dto = EnrolledCourseDTO.builder()
                    .id(courseEnr.getId())
                    .name(courseEnr.getName())
                    .description(courseEnr.getDescription())
                    .code(courseEnr.getCode())
                    .image(courseEnr.getImage())
                    .lastAccessed(enrollment.getEnrollmentDate()) // hoặc course.getLastAccessed() nếu có
                    .build();

            enrolledCourses.add(dto);
        }

        Set<Long> enrolledCourseIds = enrolledCourses.stream()
                .map(EnrolledCourseDTO::getId)
                .collect(Collectors.toSet());

        model.addAttribute("course", course);
        model.addAttribute("sessionsWithMaterials", course.getSessions().stream().map(session -> {
            List<CourseMaterial> materials = courseMaterialService.findBySessionId(session.getId());
            return new SessionDTO(session, materials);
        }).collect(Collectors.toList()));
        model.addAttribute("enrolledCourseIds", enrolledCourseIds);
        model.addAttribute("content", "student_learnings/course_detail");

        return "LAYOUT";
    }

    @PostMapping("/courses/{courseId}/enroll")
    public String enrollStudent(@PathVariable Long courseId, HttpServletRequest request, Principal principal, RedirectAttributes redirectAttributes) {
        String token = jwtTokenUtils.extractTokenFromRequest(request);
        Long studentId = jwtTokenUtils.extractUserId(token);
        courseEnrollmentService.enrollStudents(courseId, List.of(studentId));

        // Thêm thông báo thành công
        redirectAttributes.addFlashAttribute("enrollMessage", "You have successfully enrolled in the course!");

        return "redirect:/student/courses/{courseId}";
    }

    private List<EnrolledCourseDTO> GetEnrolledCourse(Course course, HttpServletRequest request) {
        // Fetch materials for each session and sort them using CourseMaterialService
        for (Session session : course.getSessions()) {
            List<CourseMaterial> sortedMaterials = courseMaterialService.findBySessionId(session.getId());
        }

        String token = jwtTokenUtils.extractTokenFromRequest(request);
        Long studentId = jwtTokenUtils.extractUserId(token);

        List<EnrolledCourseDTO> enrolledCoursesByLeaningPath = userService.getEnrolledCourses(studentId);
//        List<EnrolledCourseDTO> enrolledCoursesByTrainingProgram = trainingProgramEnrollmentService.getStudentCourses(studentId);
        List<EnrolledCourseDTO> enrolledCoursesByClass = classesService.getStudentCoursesFromClasses(studentId);
        List<CourseEnrollment> enrolledSingleCourses = courseEnrollmentService.getEnrollmentsByUser(studentId);


        List<EnrolledCourseDTO> enrolledCourses = new ArrayList<>();
        enrolledCourses.addAll(enrolledCoursesByLeaningPath);
//        enrolledCourses.addAll(enrolledCoursesByTrainingProgram);
        enrolledCourses.addAll(enrolledCoursesByClass);
        for (CourseEnrollment enrollment : enrolledSingleCourses) {
            Course courseEnr = enrollment.getCourse();
            EnrolledCourseDTO dto = EnrolledCourseDTO.builder()
                    .id(courseEnr.getId())
                    .name(courseEnr.getName())
                    .description(courseEnr.getDescription())
                    .code(courseEnr.getCode())
                    .image(courseEnr.getImage())
                    .lastAccessed(enrollment.getEnrollmentDate()) // hoặc course.getLastAccessed() nếu có
                    .build();

            enrolledCourses.add(dto);
        }
        return enrolledCourses;
    }
}

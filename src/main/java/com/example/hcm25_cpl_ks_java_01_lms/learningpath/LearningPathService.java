package com.example.hcm25_cpl_ks_java_01_lms.learningpath;

import com.example.hcm25_cpl_ks_java_01_lms.course.Course;
import com.example.hcm25_cpl_ks_java_01_lms.course.CourseRepository;
import com.example.hcm25_cpl_ks_java_01_lms.course_enrollment.CourseEnrollment;
import com.example.hcm25_cpl_ks_java_01_lms.course_enrollment.CourseEnrollmentService;
import com.example.hcm25_cpl_ks_java_01_lms.learningpath_course.LearningPathCourse;
import com.example.hcm25_cpl_ks_java_01_lms.learningpath_course.LearningPathCourseRepository;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningPathService {

    private final LearningPathRepository learningPathRepository;

    private final CourseRepository courseRepository;

    private final UserRepository userRepository;

    private final LearningPathCourseRepository learningPathCourseRepository;
    private final CourseEnrollmentService courseEnrollmentService;


    public Page<LearningPath> getAllLearningPaths(Pageable pageable) {
        return learningPathRepository.findAll(pageable);
    }

    public void createLearningPath(LearningPath learningPath, List<Long> selectedCourseIds) {
        List<Course> selectedCourses = courseRepository.findAllById(selectedCourseIds);
        learningPath.setCourses(selectedCourses);
        learningPathRepository.save(learningPath);
    }

    @Transactional
    public void createLearningPathWithOrder(LearningPath learningPath, List<Long> selectedCourseIds, List<Long> courseOrder) {
        LearningPath savedLearningPath = learningPathRepository.save(learningPath);
        if (selectedCourseIds != null && !selectedCourseIds.isEmpty()) {
            for (int i = 0; i < selectedCourseIds.size(); i++) {
                Long courseId = selectedCourseIds.get(i);
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
                // Tìm index của courseId trong courseOrder để lấy thứ tự
                int orderIndex = courseOrder.indexOf(courseId);
                LearningPathCourse learningPathCourse = LearningPathCourse.builder()
                        .learningPathId(savedLearningPath.getId())
                        .courseId(course.getId())
                        .orderNumber(orderIndex != -1 ? (long)orderIndex + 1 : null) // Lưu thứ tự (index + 1), cho phép null nếu không tìm thấy
                        .build();
                learningPathCourseRepository.save(learningPathCourse);
            }
        }
    }

    public LearningPath getLearningPathById(Long id) {
        Optional<LearningPath> optionalLearningPath = learningPathRepository.findById(id);
        return optionalLearningPath.orElse(null);
    }

    public void updateLearningPath(Long id, LearningPath learningPath, List<Long> selectedCourseIds) {
        LearningPath existingLearningPath = getLearningPathById(id);
        if (existingLearningPath != null) {
            existingLearningPath.setName(learningPath.getName());
            existingLearningPath.setDescription(learningPath.getDescription());
            List<Course> selectedCourses = courseRepository.findAllById(selectedCourseIds);
            existingLearningPath.setCourses(selectedCourses);
            learningPathRepository.save(existingLearningPath);
        }
    }

    @Transactional
    public void updateLearningPathWithOrder(Long id, LearningPath learningPath, List<Long> selectedCourseIds, List<Long> courseOrder) {
        LearningPath existingLearningPath = getLearningPathById(id);
        existingLearningPath.setName(learningPath.getName());
        existingLearningPath.setDescription(learningPath.getDescription());
        learningPathRepository.save(existingLearningPath);

        // Xóa các khóa học hiện tại của learning path
        learningPathCourseRepository.deleteByLearningPathId(id);

        // Thêm lại các khóa học đã chọn với thứ tự mới
        if (selectedCourseIds != null && !selectedCourseIds.isEmpty()) {
            for (int i = 0; i < selectedCourseIds.size(); i++) {
                Long courseId = selectedCourseIds.get(i);
                Course course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new RuntimeException("Course not found with id: " + courseId));
                int orderIndex = courseOrder.indexOf(courseId);
                LearningPathCourse learningPathCourse = LearningPathCourse.builder()
                        .learningPathId(id)
                        .courseId(course.getId())
                        .orderNumber(orderIndex != -1 ? (long) orderIndex + 1 : null)
                        .build();
                learningPathCourseRepository.save(learningPathCourse);
            }
        }
    }

    public void deleteLearningPath(Long id) {
        learningPathRepository.deleteById(id);
    }

    public Page<LearningPath> searchLearningPaths(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return learningPathRepository.findAll(pageable);
        } else {
            return learningPathRepository.findByNameContainingIgnoreCase(searchTerm, pageable);
        }
    }
//    public void enrollUserInLearningPath(Long userId, Long learningPathId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        LearningPath learningPath = findById(learningPathId);
//
//        // Kiểm tra xem người dùng đã đăng ký Learning Path này chưa
//        if (user.getLearningPaths().contains(learningPath)) {
//            throw new RuntimeException("Already enrolled in this Learning Path");
//        }
//        user.getLearningPaths().add(learningPath);
//        userRepository.save(user);
//
//        // Lấy danh sách các Course thuộc Learning Path
//        List<LearningPathCourse> learningPathCourses = learningPathCourseRepository.findByLearningPath(learningPath);
//        List<Course> coursesInLearningPath = learningPathCourses.stream()
//                .map(LearningPathCourse::getCourse)
//                .toList();
//
//        // Lấy danh sách các Course mà người dùng đã đăng ký
//        List<CourseEnrollment> existingEnrollments = courseEnrollmentService.findByUser(user);
//        List<Course> enrolledCourses = existingEnrollments.stream()
//                .map(CourseEnrollment::getCourse)
//                .toList();
//
//        // Xác định các Course mà người dùng chưa đăng ký trong Learning Path
//        List<Course> newCoursesToEnroll = coursesInLearningPath.stream()
//                .filter(course -> !enrolledCourses.contains(course))
//                .toList();
//
//        // Tự động đăng ký người dùng vào các Course mới
//        courseEnrollmentService.enrollStudents(newCoursesToEnroll, List.of(userId));
//    }

    public void enrollUserInLearningPath(Long userId, Long learningPathId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LearningPath learningPath = findById(learningPathId);

        // Kiểm tra xem người dùng đã đăng ký Learning Path này chưa
        if (user.getLearningPaths().contains(learningPath)) {
            throw new RuntimeException("Already enrolled in this Learning Path");
        }
        user.getLearningPaths().add(learningPath);
        userRepository.save(user);

        // Lấy danh sách các Course thuộc Learning Path
        List<LearningPathCourse> learningPathCourses = learningPathCourseRepository.findByLearningPath(learningPath);
        List<Course> coursesInLearningPath = learningPathCourses.stream()
                .map(LearningPathCourse::getCourse)
                .toList();

        // Lấy danh sách các Course mà người dùng đã đăng ký
        List<CourseEnrollment> existingEnrollments = courseEnrollmentService.findByUser(user);
        List<Course> enrolledCourses = existingEnrollments.stream()
                .map(CourseEnrollment::getCourse)
                .toList();

        // Xác định các Course mà người dùng chưa đăng ký trong Learning Path
        List<Course> newCoursesToEnroll = coursesInLearningPath.stream()
                .filter(course -> !enrolledCourses.contains(course))
                .toList();

        // Tự động đăng ký người dùng vào các Course mới
        courseEnrollmentService.enrollStudents(newCoursesToEnroll, List.of(userId));
    }


    public List<LearningPath> getLearningPathsByUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            return user.getLearningPaths();
        }
        return null; // Hoặc trả về danh sách rỗng nếu người dùng không tồn tại
    }


    public Page<LearningPath> getLearningPathsByUser(Long userId, String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return learningPathRepository.findByUser_Id(userId, pageable);
        } else {
            return learningPathRepository.findByUsers_IdAndNameContainingIgnoreCase(userId, searchTerm, pageable);
        }
    }
    public void unenrollUserFromLearningPath(Long userId, Long learningPathId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        LearningPath learningPath = findById(learningPathId);

        // Kiểm tra xem người dùng đã đăng ký Learning Path này chưa
        if (!user.getLearningPaths().contains(learningPath)) {
            throw new RuntimeException("User is not enrolled in this Learning Path");
        }

        user.getLearningPaths().remove(learningPath);
        userRepository.save(user);

        // Lấy danh sách các Course thuộc Learning Path
        List<LearningPathCourse> learningPathCourses = learningPathCourseRepository.findByLearningPath(learningPath);
        List<Course> coursesInLearningPath = learningPathCourses.stream()
                .map(LearningPathCourse::getCourse)
                .toList();

        // Hủy đăng ký người dùng khỏi các Course thuộc Learning Path
        courseEnrollmentService.unenrollStudentsFromCourses(userId, coursesInLearningPath);
    }

    private LearningPath findById(Long id) {
        return learningPathRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning Path not found"));
    }


    public ByteArrayInputStream generateExcel(List<LearningPath> learningPaths) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Learning Paths");

            // Tạo header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerCellStyle.setFont(font);

            String[] headers = {"ID", "Name", "Description", "Course IDs"};
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Tạo data rows
            int rowIdx = 1;
            for (LearningPath learningPath : learningPaths) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(learningPath.getId());
                row.createCell(1).setCellValue(learningPath.getName());
                row.createCell(2).setCellValue(learningPath.getDescription());

                // Lấy danh sách Course IDs
                String courseIds = learningPath.getCourses().stream()
                        .map(course -> course.getId().toString())
                        .collect(Collectors.joining(", "));
                row.createCell(3).setCellValue(courseIds);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public List<LearningPath> getEnrolledLearningPaths(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getLearningPaths();
    }

    public List<LearningPath> getAllLearningPaths() {
        return learningPathRepository.findAll();
    }

    public void saveAll(List<LearningPath> learningPaths) {
        learningPathRepository.saveAll(learningPaths);
    }

    public boolean isUserEnrolled(Long userId, Long learningPathId) {
        return userRepository.isUserEnrolledInLearningPath(userId, learningPathId);
    }

    public LearningPath getLearningPathWithOrderedCourses(Long id) {
        LearningPath learningPath = learningPathRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Learning Path not found"));

        // Lấy danh sách các LearningPathCourse đã được sắp xếp theo orderNumber
        List<LearningPathCourse> learningPathCourses = learningPathCourseRepository.findByLearningPathIdOrderByOrderNumberAsc(id);

        // Tạo một danh sách mới chứa thông tin về Course và số thứ tự
        List<CourseWithOrder> coursesWithOrder = learningPathCourses.stream()
                .map(lpCourse -> {
                    Course course = lpCourse.getCourse();
                    Long orderNumber = lpCourse.getOrderNumber();
                    return new CourseWithOrder(course, orderNumber);
                })
                .collect(Collectors.toList());

        // Gán danh sách đã sắp xếp và chứa số thứ tự vào LearningPath
        learningPath.setCoursesWithOrder(coursesWithOrder);
        return learningPath;
    }

    public LearningPath getLearningPathByIdWithOrder(Long id) {
        LearningPath learningPath = getLearningPathById(id);
        List<LearningPathCourse> learningPathCourses = learningPathCourseRepository.findByLearningPathIdOrderByOrderNumberAsc(id);
        List<CourseWithOrder> coursesWithOrder = learningPathCourses.stream()
                .map(lpCourse -> new CourseWithOrder(lpCourse.getCourse(), lpCourse.getOrderNumber()))
                .collect(Collectors.toList());
        learningPath.setCoursesWithOrder(coursesWithOrder);
        return learningPath;
    }

    @Transactional
    public void importLearningPathsWithOrder(List<LearningPathExcelImporter.LearningPathWithOrder> learningPathsWithOrderList) {
        for (LearningPathExcelImporter.LearningPathWithOrder learningPathWithOrder : learningPathsWithOrderList) {
            LearningPath learningPath = learningPathRepository.save(learningPathWithOrder.getLearningPath());
            for (LearningPathExcelImporter.CourseWithOrderInfo courseWithOrderInfo : learningPathWithOrder.getCourseOrderInfos()) {
                LearningPathCourse learningPathCourse = LearningPathCourse.builder()
                        .learningPathId(learningPath.getId())
                        .courseId(courseWithOrderInfo.getCourse().getId())
                        .orderNumber(courseWithOrderInfo.getOrderNumber())
                        .build();
                learningPathCourseRepository.save(learningPathCourse);
            }
        }
    }

    // Inner class để chứa thông tin về Course và số thứ tự
    @Getter
    @Setter
    @AllArgsConstructor
    public static class CourseWithOrder {
        private Course course;
        private Long orderNumber;
    }
}

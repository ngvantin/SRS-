package com.example.hcm25_cpl_ks_java_01_lms.user_enrollment_training_program;
import com.example.hcm25_cpl_ks_java_01_lms.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/training-program-user-enrollments")
public class UserEnrollmentTrainingProgramController {

    private final UserEnrollmentTrainingProgramService userEnrollmentTrainingProgramService;

    @Autowired
    public UserEnrollmentTrainingProgramController(UserEnrollmentTrainingProgramService userEnrollmentTrainingProgramService) {
        this.userEnrollmentTrainingProgramService = userEnrollmentTrainingProgramService;
    }

    // Hiển thị danh sách user đã đăng ký chương trình đào tạo
    @GetMapping
    public String listEnrollments(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  @RequestParam(required = false) String searchTerm) {

        Page<UserEnrollmentTrainingProgram> enrollments =
                userEnrollmentTrainingProgramService.getAllEnrollments(searchTerm, page, size);

        model.addAttribute("userEnrollments", enrollments);
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("content", "userenrollmenttrainingprogram/list"); // Thymeleaf fragment
        return Constants.LAYOUT;
    }

    // Xoá 1 bản ghi theo ID
    @GetMapping("/delete/{id}")
    public String deleteEnrollment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userEnrollmentTrainingProgramService.deleteEnrollmentById(id);
            redirectAttributes.addFlashAttribute("success", "Enrollment deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting enrollment: " + e.getMessage());
        }
        return "redirect:/training-program-user-enrollments";
    }

    // Xoá nhiều bản ghi
    @PostMapping("/delete/bulk")
    public String deleteEnrollments(@RequestParam("enrollmentIds") List<Long> ids,
                                    RedirectAttributes redirectAttributes) {
        try {
            userEnrollmentTrainingProgramService.deleteEnrollmentsByIds(ids);
            redirectAttributes.addFlashAttribute("success", "Selected enrollments deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Bulk deletion failed: " + e.getMessage());
        }
        return "redirect:/training-program-user-enrollments";
    }

    // Export (tạm thời chưa triển khai)
    @GetMapping("/export")
    public String export(Model model) {
        model.addAttribute("error", "Export functionality not yet implemented.");
        return "redirect:/training-program-user-enrollments";
    }
}
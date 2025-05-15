package com.example.hcm25_cpl_ks_java_01_lms.student;

import com.example.hcm25_cpl_ks_java_01_lms.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
@RequestMapping("/students")
public class StudentViewController {
    @Autowired
    private StudentService studentService;

    // list table
    @GetMapping
    public String showStudentListPage(Model model) {
        model.addAttribute("content", "students/list");
        return Constants.LAYOUT;
    }

    // create new student
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("content", "students/create"); // file create.html
        return Constants.LAYOUT;
    }

    // edit student
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Student> optionalStudent = studentService.findStudentById(id);

        if (optionalStudent.isEmpty()) {
            model.addAttribute("error", "Student not found");
            model.addAttribute("content", "students/list"); // fallback nếu lỗi
            return Constants.LAYOUT;
        }

        model.addAttribute("student", optionalStudent.get());
        model.addAttribute("content", "students/edit"); // file edit.html
        return Constants.LAYOUT;
    }

    //detail student
    @GetMapping("/detail/{id}")
    public String showStudentDetailPage(@PathVariable Long id, Model model) {
        model.addAttribute("content", "students/detail");
        return Constants.LAYOUT;
    }

    @GetMapping("/print")
    public String showPrintPage() {
        return "students/print";
    }

    // create card
    @GetMapping("/create-card/{id}")
    public String showIssueCardPage(@PathVariable Long id, Model model) {
        Optional<Student> optionalStudent = studentService.findStudentById(id);

        if (optionalStudent.isEmpty()) {
            model.addAttribute("error", "Student not found");
            model.addAttribute("content", "students/list"); // fallback nếu lỗi
            return Constants.LAYOUT;
        }

        model.addAttribute("student", optionalStudent.get());
        model.addAttribute("content", "students/create-card"); // file create-card.html
        return Constants.LAYOUT;
    }

    // edit card
    @GetMapping("/edit-card/{id}")
    public String showEditCardForm(@PathVariable Long id, Model model) {
        Optional<StudentCard> optionalStudentCard = studentService.findByUserId(id);

        if (optionalStudentCard.isEmpty()) {
            model.addAttribute("error", "Student card not found");
            model.addAttribute("content", "students/list"); // fallback
            return Constants.LAYOUT;
        }

        StudentCard studentCard = optionalStudentCard.get();
        Student student = studentCard.getStudent();

        StudentDTO dto = StudentDTO.builder()
                .id(student.getId())
                .studentCode(student.getStudentCode())
                .username(student.getUsername())
                .email(student.getEmail())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .phoneNumber(student.getPhoneNumber())
                .address(student.getAddress())
                .is2faEnabled(student.getIs2faEnabled())
                .isLocked(student.getIsLocked())
                .cardNumber(studentCard.getCardNumber())
                .badgeHolderTitle(studentCard.getBadgeHolderTitle())
                .issuedAt(studentCard.getIssuedAt())
                .build();

        model.addAttribute("student", dto);
        model.addAttribute("content", "students/edit-card");
        return Constants.LAYOUT;
    }
}


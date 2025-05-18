package com.example.hcm25_cpl_ks_java_01_lms.student;

import com.example.hcm25_cpl_ks_java_01_lms.common.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/students")
public class StudentViewController {
    @Autowired
    private StudentService studentService;

    @GetMapping
    public String showStudentListPage(Model model) {
        model.addAttribute("content", "students/list");
        return Constants.LAYOUT;
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("content", "students/create"); // file create.html
        return Constants.LAYOUT;
    }

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

    @GetMapping("/detail/{id}")
    public String showStudentDetailPage(@PathVariable Long id, Model model) {
        model.addAttribute("content", "students/detail");
        return Constants.LAYOUT;
    }

    @GetMapping("/print")
    public String showPrintPage() {
        return "students/print";
    }
}


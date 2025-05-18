package com.example.hcm25_cpl_ks_java_01_lms.student;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    public Page<Student> findAllStudents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return studentRepository.findAll(pageable);
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Page<Student> searchStudents(String keyword, int page, int size) {
        return studentRepository.searchStudents(keyword, PageRequest.of(page, size));
    }

    public Page<Student> findAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    public Page<Student> searchStudents(String keyword, Pageable pageable) {
        return studentRepository.searchStudents(keyword, pageable);
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }

    public Optional<Student> findStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public List<Student> findByIds(List<Long> ids) {
        return studentRepository.findAllById(ids);
    }

    public List<DuplicateInfo> saveAllStudents(List<Student> students) {
        List<DuplicateInfo> duplicates = new ArrayList<>();

        for (Student student : students) {
            List<String> duplicateFields = new ArrayList<>();

            if (studentRepository.existsByPhoneNumber(student.getPhoneNumber())) {
                duplicateFields.add("Phone Number");
            }

            if (studentRepository.existsByStudentID(student.getStudentID())) {
                duplicateFields.add("Student ID");
            }

            if (studentRepository.existsByEmail(student.getEmail())) {
                duplicateFields.add("Email");
            }

            if (!duplicateFields.isEmpty()) {
                duplicates.add(new DuplicateInfo(student, duplicateFields));
            } else {
                studentRepository.save(student);
            }
        }

        return duplicates;
    }

    public long getTotalStudents() {
        return studentRepository.count();
    }
}

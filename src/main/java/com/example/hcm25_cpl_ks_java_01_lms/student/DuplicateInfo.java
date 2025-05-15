package com.example.hcm25_cpl_ks_java_01_lms.student;

import java.util.List;

public class DuplicateInfo {
    private Student student;
    private List<String> duplicateFields;

    public DuplicateInfo() {}

    public DuplicateInfo(Student student, List<String> duplicateFields) {
        this.student = student;
        this.duplicateFields = duplicateFields;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public List<String> getDuplicateFields() {
        return duplicateFields;
    }

    public void setDuplicateFields(List<String> duplicateFields) {
        this.duplicateFields = duplicateFields;
    }
}


package com.example.hcm25_cpl_ks_java_01_lms.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByStudentID(String studentID);
    boolean existsByEmail(String email);

    @Query("SELECT s FROM Student s WHERE " +
            "(LOWER(s.studentID) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            ":keyword IS NOT NULL")
    Page<Student> searchStudents(@Param("keyword") String keyword, Pageable pageable);

    Page<Student> findAll(Pageable pageable);

    List<Student> findAll();
}

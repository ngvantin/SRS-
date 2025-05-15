package com.example.hcm25_cpl_ks_java_01_lms.student;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByStudentCode(String studentCode);
    boolean existsByEmail(String email);

    @Query("SELECT new com.example.hcm25_cpl_ks_java_01_lms.student.StudentDTO(" +
            "s.id, s.username, s.email, s.studentCode, s.firstName, s.lastName, " +
            "s.phoneNumber, s.address, s.is2faEnabled, s.isLocked, " +
            "sc.cardNumber, sc.badgeHolderTitle, sc.issuedAt) " +
            "FROM StudentCard sc LEFT JOIN sc.student s " +
            "WHERE s.id = :userId")
    Optional<StudentDTO> findStudentDTOByUserId(Long userId);

    @Query("SELECT s FROM Student s " +
            "LEFT JOIN StudentCard sc ON s.id = sc.student.id " +
            "WHERE " +
            "(LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(sc.cardNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            ":keyword IS NOT NULL")
    Page<Student> searchStudents(@Param("keyword") String keyword, Pageable pageable);

    Optional<Student> findByStudentCode(String studentCode);

    Page<Student> findAll(Pageable pageable);

    List<Student> findAll();
}

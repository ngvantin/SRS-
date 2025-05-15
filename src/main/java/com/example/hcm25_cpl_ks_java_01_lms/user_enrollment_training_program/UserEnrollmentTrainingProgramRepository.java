package com.example.hcm25_cpl_ks_java_01_lms.user_enrollment_training_program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserEnrollmentTrainingProgramRepository extends JpaRepository<UserEnrollmentTrainingProgram, Long> {

    // Tìm theo chương trình đào tạo
    List<UserEnrollmentTrainingProgram> findByTrainingProgramId(Long trainingProgramId);

    // Tìm theo người dùng
    List<UserEnrollmentTrainingProgram> findByUserId(Long userId);

    // Tìm kiếm theo tên chương trình (giả sử dùng trong search)
    Page<UserEnrollmentTrainingProgram> findByTrainingProgram_ProgramNameContainingIgnoreCase(String programName, Pageable pageable);

    // Kiểm tra đã tồn tại user trong chương trình chưa (tránh tạo trùng)
    boolean existsByUserIdAndTrainingProgramId(Long userId, Long trainingProgramId);
}
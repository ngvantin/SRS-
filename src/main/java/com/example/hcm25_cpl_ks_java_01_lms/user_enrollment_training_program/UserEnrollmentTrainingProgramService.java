package com.example.hcm25_cpl_ks_java_01_lms.user_enrollment_training_program;
import com.example.hcm25_cpl_ks_java_01_lms.training_program.TrainingProgram;
import com.example.hcm25_cpl_ks_java_01_lms.training_program.TrainingProgramRepository;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserEnrollmentTrainingProgramService {

    private final UserEnrollmentTrainingProgramRepository enrollmentRepository;
    private final TrainingProgramRepository trainingProgramRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserEnrollmentTrainingProgramService(UserEnrollmentTrainingProgramRepository enrollmentRepository,
                                                TrainingProgramRepository trainingProgramRepository,
                                                UserRepository userRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.trainingProgramRepository = trainingProgramRepository;
        this.userRepository = userRepository;
    }

    // Ghi danh nhiều user vào một chương trình
    public void enrollUsers(Long trainingProgramId, List<Long> userIds) {
        Optional<TrainingProgram> trainingProgramOpt = trainingProgramRepository.findById(trainingProgramId);

        if (trainingProgramOpt.isEmpty()) {
            throw new RuntimeException("Training Program not found with ID: " + trainingProgramId);
        }

        TrainingProgram trainingProgram = trainingProgramOpt.get();

        for (Long userId : userIds) {
            Optional<User> userOpt = userRepository.findById(userId);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Kiểm tra trùng lặp
                if (!enrollmentRepository.existsByUserIdAndTrainingProgramId(user.getId(), trainingProgramId)) {
                    UserEnrollmentTrainingProgram enrollment = UserEnrollmentTrainingProgram.builder()
                            .user(user)
                            .trainingProgram(trainingProgram)
                            .enrollmentDate(LocalDateTime.now())
                            .build();

                    enrollmentRepository.save(enrollment);
                }
            }
        }
    }

    // Lấy tất cả bản ghi có phân trang + tìm kiếm
    public Page<UserEnrollmentTrainingProgram> getAllEnrollments(String searchTerm, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);

        if (searchTerm != null && !searchTerm.isEmpty()) {
            return enrollmentRepository.findByTrainingProgram_ProgramNameContainingIgnoreCase(searchTerm, pageRequest);
        }

        return enrollmentRepository.findAll(pageRequest);
    }

    // Tìm theo chương trình
    public List<UserEnrollmentTrainingProgram> getEnrollmentsByProgram(Long trainingProgramId) {
        return enrollmentRepository.findByTrainingProgramId(trainingProgramId);
    }

    // Tìm theo user
    public List<UserEnrollmentTrainingProgram> getEnrollmentsByUser(Long userId) {
        return enrollmentRepository.findByUserId(userId);
    }

    // Lấy danh sách user theo chương trình
    public List<User> findAllUsersByProgramId(Long programId) {
        return enrollmentRepository.findByTrainingProgramId(programId)
                .stream()
                .map(UserEnrollmentTrainingProgram::getUser)
                .collect(Collectors.toList());
    }

    // Xoá một bản ghi
    public void deleteEnrollmentById(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Enrollment with ID " + id + " not found.");
        }
        enrollmentRepository.deleteById(id);
    }

    // Xoá nhiều bản ghi
    public void deleteEnrollmentsByIds(List<Long> ids) {
        for (Long id : ids) {
            if (enrollmentRepository.existsById(id)) {
                enrollmentRepository.deleteById(id);
            }
        }
    }
}
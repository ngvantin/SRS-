package com.example.hcm25_cpl_ks_java_01_lms.user_enrollment_training_program;

import com.example.hcm25_cpl_ks_java_01_lms.training_program.TrainingProgram;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class UserEnrollmentTrainingProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "training_program_id", referencedColumnName = "id", nullable = false)
    private TrainingProgram trainingProgram;

    @Column(nullable = false)
    private LocalDateTime enrollmentDate;

    @Override
    public String toString() {
        return "UserEnrollmentTrainingProgram{" +
                "user=" + user.getUsername() +
                ", trainingProgram=" + trainingProgram.getProgramName() +
                ", enrollmentDate=" + enrollmentDate +
                '}';
    }
}
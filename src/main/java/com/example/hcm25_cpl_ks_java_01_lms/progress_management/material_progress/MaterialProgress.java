package com.example.hcm25_cpl_ks_java_01_lms.progress_management.material_progress;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class MaterialProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Long sessionId;

    @Column(nullable = false)
    private Long materialId;

    @Column(nullable = false)
    private Boolean isCompleted = false;

    @Column(nullable = true)
    private LocalDateTime completedAt;

    // Thời gian học dự kiến (ví dụ: 15 phút, 30 phút)
    @Column(nullable = true)
    private Duration estimatedStudyTime;

    // Tổng thời gian học thực tế đã dành cho material này
    @Column(nullable = true)
    private Duration totalStudyTime;

    // Số lần truy cập vào material
    @Column(nullable = true)
    private Integer accessCount = 0;

    // Thời gian truy cập gần nhất
    @Column(nullable = true)
    private LocalDateTime lastAccessedAt;
}
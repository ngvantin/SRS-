package com.example.hcm25_cpl_ks_java_01_lms.progress_management;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class ProgressManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long courseId;

    @Column(nullable = false)
    private Float progressPercent;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}


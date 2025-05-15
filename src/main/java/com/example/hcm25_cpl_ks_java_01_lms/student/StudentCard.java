package com.example.hcm25_cpl_ks_java_01_lms.student;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "student_cards")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StudentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String cardNumber;

    @Column(nullable = false, length = 50)
    private String badgeHolderTitle;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private LocalDate issuedAt;

    @Column
    private LocalDate revokedAt;

    @OneToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private Student student;
}

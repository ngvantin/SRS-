package com.example.hcm25_cpl_ks_java_01_lms.student;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10, unique = true)
    private String studentID;

    @Column(nullable = false, length = 255)
    private String lastName;

    @Column(nullable = false, length = 255)
    private String firstName;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String phoneNumber;

    @Column(length = 255)
    private String address;
}

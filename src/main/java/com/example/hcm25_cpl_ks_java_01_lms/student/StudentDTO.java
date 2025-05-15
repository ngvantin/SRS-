package com.example.hcm25_cpl_ks_java_01_lms.student;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO {
    private Long id;
    private String username;
    private String password;
    private String email;
    private String studentCode;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private Boolean is2faEnabled;
    private Boolean isLocked;

    private String cardNumber;
    private String badgeHolderTitle;
    private LocalDate issuedAt;
}



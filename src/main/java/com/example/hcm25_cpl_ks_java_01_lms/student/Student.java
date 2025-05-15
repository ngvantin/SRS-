package com.example.hcm25_cpl_ks_java_01_lms.student;

import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")  // Định nghĩa bảng cho lớp Student
@PrimaryKeyJoinColumn(name = "user_id") // Liên kết với khóa chính của bảng User
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Student extends User {
    @Column(length = 10, unique = true)
    private String studentCode;

    @Column(length = 255)
    private String phoneNumber;

    @Column(length = 255)
    private String address;

    public void setUser(User user) {
        this.setId(user.getId());
        this.setUsername(user.getUsername());
        this.setPassword(user.getPassword());
        this.setFirstName(user.getFirstName());
        this.setLastName(user.getLastName());
        this.setEmail(user.getEmail());
        this.setIs2faEnabled(user.getIs2faEnabled());
        this.setIsLocked(user.getIsLocked());
        this.setRoles(user.getRoles());
        this.setConversations(user.getConversations());
        this.setMessages(user.getMessages());
        this.setActivities(user.getActivities());
        this.setLearningPaths(user.getLearningPaths());
    }

    // Hàm khởi tạo lớp Student với các thông tin bổ sung
    public Student(User user, String studentCode, String phoneNumber, String address) {
        super(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getIs2faEnabled(),
                user.getIsLocked(),
                user.getRoles(),
                user.getConversations(),
                user.getMessages(),
                user.getActivities(),
                user.getLearningPaths()
        );
        this.studentCode = studentCode;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}

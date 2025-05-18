package com.example.hcm25_cpl_ks_java_01_lms.user;

import com.example.hcm25_cpl_ks_java_01_lms.activity.Activity;
import com.example.hcm25_cpl_ks_java_01_lms.chat.Conversation;
import com.example.hcm25_cpl_ks_java_01_lms.chat.Message;
import com.example.hcm25_cpl_ks_java_01_lms.learningpath.LearningPath;
import com.example.hcm25_cpl_ks_java_01_lms.role.Role;
import com.example.hcm25_cpl_ks_java_01_lms.user.dto.UserDTO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Builder
@Table(name = "app_user")  // Renaming 'user' to 'app_user'
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, updatable = false)
    private Boolean is2faEnabled = false;

    @Column(nullable = false)
    private Boolean isLocked = false;

    // Many-to-many relationship with Role
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "user_conversations",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "conversation_id")
    )
    private List<Conversation> conversations;

    @OneToMany(mappedBy="user")
    private List<Message> messages;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_activities", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "activity_id"))
    private List<Activity> activities;


    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_learning_paths",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "learning_path_id")
    )
    private List<LearningPath> learningPaths;

    @Override
    public String toString() {
        return username + " - " + (Boolean.TRUE.equals(isLocked) ? "Locked" : "Active");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorityList = new ArrayList<>();
        if(roles != null)
            authorityList.add(new SimpleGrantedAuthority("ROLE_" + roles.get(0).getName()));
        return authorityList;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public UserDTO toDTO() {
        if(roles == null)
            roles = new ArrayList<>();

        return UserDTO.builder()
                .id(id)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .is2faEnabled(is2faEnabled)
                .isLocked(isLocked)
                .roles(roles.stream().map(Role::toDTO).collect(Collectors.toList()))
                .build();
    }
    public String getFullName() {
        return this.firstName + " " + this.lastName;
    }

    public String getStudentCode() {
        return this.username;
    }


}


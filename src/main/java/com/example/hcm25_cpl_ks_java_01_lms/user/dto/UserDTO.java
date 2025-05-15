package com.example.hcm25_cpl_ks_java_01_lms.user.dto;

import com.example.hcm25_cpl_ks_java_01_lms.role.dto.RoleDTO;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Boolean is2faEnabled;
    private Boolean isLocked;
    private List<RoleDTO> roles;

    public User toEntity() {
        if(roles == null)
            roles = new ArrayList<>();

        return User.builder()
                .id(id)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(password)
                .is2faEnabled(is2faEnabled)
                .isLocked(isLocked)
                .roles(roles.stream().map(RoleDTO::toEntity).collect(Collectors.toList()))
                .build();
    }
}

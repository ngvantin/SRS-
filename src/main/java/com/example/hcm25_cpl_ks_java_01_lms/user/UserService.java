package com.example.hcm25_cpl_ks_java_01_lms.user;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.example.hcm25_cpl_ks_java_01_lms.chat.UserDTO;
import com.example.hcm25_cpl_ks_java_01_lms.course.Course;
import com.example.hcm25_cpl_ks_java_01_lms.student.Student;
import com.example.hcm25_cpl_ks_java_01_lms.student.StudentRepository;
import com.example.hcm25_cpl_ks_java_01_lms.student_learning.dto.EnrolledCourseDTO;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hcm25_cpl_ks_java_01_lms.role.Role;
import com.example.hcm25_cpl_ks_java_01_lms.role.RoleRepository;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Autowired
    private StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return userRepository.findById(Long.valueOf(userId)).orElseThrow(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> getAllUsers(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (searchTerm != null && !searchTerm.isEmpty()) {
            return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm, pageable);
        }
        return userRepository.findAll(pageable);
    }


    public Page<User> getAllUsers(String searchTerm, Pageable pageable) {
        if (searchTerm != null && !searchTerm.isEmpty()) {
            return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(searchTerm, searchTerm, pageable);
        }
        return userRepository.findAll(pageable);
    }

    public void saveAll(List<User> users) {
        userRepository.saveAll(users);
    }

    public User createUser(User user) {
        if (userRepository.existsByUsernameOrEmail(user.getUsername(), user.getEmail())) {
            throw new UserExistedException("Username or email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        List<Role> roles = user.getRoles();
        if (roles != null && !roles.isEmpty()) {
            roles = roleRepository.findByListId(user.getRoles().stream().map(Role::getId).toList());
            user.setRoles(roles);
        }

        boolean isStudent = roles.stream().anyMatch(role -> "STUDENT".equalsIgnoreCase(role.getName()));

        if (isStudent) {
            Student student = new Student();
            student.setUser(user);
            return studentRepository.save(student);
        } else {
            return userRepository.save(user);
        }
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateUser(User userDetails) {
        User existingUser = userRepository.findByUsernameOrEmail(userDetails.getUsername(), userDetails.getEmail());
        if (existingUser != null && !existingUser.getId().equals(userDetails.getId())) {
            throw new UserExistedException("Username or email already exists");
        }

        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user != null) {
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }

            user.getRoles().clear();
            if (userDetails.getRoles() != null && !userDetails.getRoles().isEmpty()) {
                List<Role> roles = roleRepository.findByListId(userDetails.getRoles().stream().map(Role::getId).toList());
                user.getRoles().addAll(roles);
            }
            
            user.setFirstName(userDetails.getFirstName());
            user.setLastName(userDetails.getLastName());
            user.setIs2faEnabled(userDetails.getIs2faEnabled());
            user.setIsLocked(userDetails.getIsLocked());
            return userRepository.save(user);
        }
        return null;
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            for (Role role : user.getRoles()) {
                List<User> users = role.getUsers();
                while(users.contains(user))
                    role.getUsers().remove(user);
                roleRepository.save(role);
            }
            user.getRoles().clear();
            userRepository.save(user);
            userRepository.deleteByUserId(id);
        }
    }

    @Transactional
    public void deleteUsersByIds(List<Long> ids) {
        for (Long id: ids) {
            deleteUser(id);
        }
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            List<Role> roles = user.getRoles();
            if(roles == null || roles.isEmpty())
                throw  new UserNotFoundException("This account has no role in the system, please contact the administrator.");
            return user;
        }
        return null;
    }

    public List<User> getUserByRole(String role) {
        return userRepository.findByRoles_Name(role).orElse(null);
    }


    public Page<User> getUserByRole(String role, Pageable pageable) {
        return userRepository.findByRoles_Name(role, pageable);
    }

   public void checkRegister(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserExistedException("Email already exists");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserExistedException("Username already exists");
        }
    }

    public void register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        roleRepository.findByName("Student").ifPresent(role -> user.setRoles(List.of(role)));
        Student student = new Student();
        student.setUser(user);
        studentRepository.save(student);
    }

    public ByteArrayInputStream exportToExcel(List<User> users) throws IOException {
        return generateExcel(users);
    }

    private ByteArrayInputStream generateExcel(List<User> users) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Users");

            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerCellStyle.setFont(font);

            String[] headers = {"ID", "Username", "Password", "First Name", "Last Name", "Email", "2FA Enabled", "Locked", "Roles"};
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            // Create data rows
            int rowIdx = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getPassword());
                row.createCell(3).setCellValue(user.getFirstName());
                row.createCell(4).setCellValue(user.getLastName());
                row.createCell(5).setCellValue(user.getEmail());
                row.createCell(6).setCellValue(user.getIs2faEnabled());
                row.createCell(7).setCellValue(user.getIsLocked());
                row.createCell(8).setCellValue(user.getRoles().stream().map(Role::getName).collect(Collectors.joining(", ")));
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    public void saveAllFromExcel(List<User> users) {
        List<User> filteredUser = UserDataExcelValidator.filterDuplicateUsers(users);
        for(User user : filteredUser) {
            if(!userRepository.existsByUsernameOrEmail(user.getUsername(), user.getEmail())) {
                List<Role> roles = roleRepository.findByListName(user.getRoles().stream().map(Role::getName).toList());
                user.setPassword(passwordEncoder.encode("1"));
                user.setRoles(roles);
                user = userRepository.save(user);

                for(Role role : roles) {
                    List<User> users1 = role.getUsers();
                    if (users1 == null)
                        users1 = new ArrayList<>();

                    if (!users1.contains(user)) {
                        users1.add(user);
                        role.setUsers(users1);
                        roleRepository.save(role);
                    }
                }
            }
        }
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    //feature/chat
    public List<User> getUsersByIds(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    public List<User> findUsersByIds(List<Long> userIds) {
        return userRepository.findAllById(userIds);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found with this email");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    public List<Map<String, Object>> getUsersOfDepartment() {
        return userRepository.findAll()
                .stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("status", user.getIsLocked() ? "Inactive" : "Active"); // Thêm trạng thái của user
                    return userMap;
                })
                .collect(Collectors.toList());
    }
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    // get student course
    public List<EnrolledCourseDTO> getEnrolledCourses(Long userId) {
        List<Course> enrolledCourses = userRepository.findEnrolledCoursesByUserId(userId);
        return enrolledCourses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private EnrolledCourseDTO convertToDTO(Course course) {
        if (course == null) {
            return null; // Hoặc trả về một DTO mặc định
        }

        EnrolledCourseDTO dto = new EnrolledCourseDTO();
        try {
            dto.setId(course.getId());
            dto.setName(course.getName());
            dto.setDescription(course.getDescription());
            dto.setCode(course.getCode());
            dto.setImage(course.getImage());
        } catch (Exception e) {
            // Ghi log lỗi hoặc xử lý lỗi theo yêu cầu
            return null; // Hoặc trả về một DTO mặc định
        }
        return dto;
    }

    public void saveAllInstructorsFromExcel(List<User> users) {
        List<User> filteredUser = UserDataExcelValidator.filterDuplicateUsers(users);
        for(User user : filteredUser) {
            user.setRoles(Collections.singletonList(Role.builder().name("Instructor").build()));
            if(!userRepository.existsByUsernameOrEmail(user.getUsername(), user.getEmail())) {
                List<Role> roles = roleRepository.findByListName(user.getRoles().stream().map(Role::getName).toList());
                user.setPassword(passwordEncoder.encode("1"));
                user.setRoles(roles);
                user = userRepository.save(user);

                for(Role role : roles) {
                    List<User> users1 = role.getUsers();
                    if (users1 == null)
                        users1 = new ArrayList<>();

                    if (!users1.contains(user)) {
                        users1.add(user);
                        role.setUsers(users1);
                        roleRepository.save(role);
                    }
                }
            }
        }
    }

    public List<User> getAllInstructors() {
        return userRepository.findByRoles_Name("Instructor").orElse(new ArrayList<>());
    }

    public List<User> getAllAdmins() {
        return userRepository.findByRoles_Name("Admin").orElse(new ArrayList<>());
    }


    public List<UserDTO> searchUsersByName(Long currentUserId, String searchTerm, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset / limit, limit);
        return userRepository.searchUsersByName(currentUserId, searchTerm.toLowerCase(), pageable)
                .stream()
                .map(user -> new UserDTO(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName()))
                .collect(Collectors.toList());
    }

    public List<User> findByRoleId(Long roleId) {
        return userRepository.findAllByRoles_Id(roleId);
    }

}


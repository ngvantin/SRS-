package com.example.hcm25_cpl_ks_java_01_lms.user.api;

import com.example.hcm25_cpl_ks_java_01_lms.activity.Activity;
import com.example.hcm25_cpl_ks_java_01_lms.activity.ActivityService;
import com.example.hcm25_cpl_ks_java_01_lms.classes.ClassesService;
import com.example.hcm25_cpl_ks_java_01_lms.config.security.JwtTokenUtils;
import com.example.hcm25_cpl_ks_java_01_lms.course_enrollment.CourseEnrollmentService;
import com.example.hcm25_cpl_ks_java_01_lms.user.User;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserExcelImporter;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserExistedException;
import com.example.hcm25_cpl_ks_java_01_lms.user.UserService;
import com.example.hcm25_cpl_ks_java_01_lms.user.dto.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("@customSecurityService.hasRoleForModule(authentication, 'User')")
@Tag(name = "Users", description = "API for managing users in the Learning Management System")
public class UserApiController {

    private final UserService userService;
    private final ActivityService activityService;
    private final ClassesService classService;
    private final CourseEnrollmentService courseEnrollmentService;
    private final JwtTokenUtils jwtTokenUtils;

    @Autowired
    public UserApiController(UserService userService, ActivityService activityService,  ClassesService classService, CourseEnrollmentService courseEnrollmentService, JwtTokenUtils jwtTokenUtils) {
        this.userService = userService;
        this.activityService = activityService;
        this.classService = classService;
        this.courseEnrollmentService = courseEnrollmentService;
        this.jwtTokenUtils = jwtTokenUtils;
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve a paginated list of all users, optionally filtered by search term")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved users", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<Page<UserDTO>> getUsers(
            @PageableDefault Pageable pageable,
            @RequestParam(required = false) @Parameter(description = "Search term to filter users by name or other attributes") String searchTerm) {
        try {
            Page<User> usersPage = userService.getAllUsers(searchTerm, pageable);
            Page<UserDTO> dtoPage = usersPage.map(User::toDTO);
            return ResponseEntity.ok(dtoPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping
    @Operation(summary = "Create a new user", description = "Add a new user to the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "User already exists or invalid data", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> createUser(@RequestBody @Parameter(description = "User details to create") UserDTO userDTO) {
        try {
            User createdUser = userService.createUser(userDTO.toEntity());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser.toDTO());
        } catch (UserExistedException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully", content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> getUserById(@PathVariable @Parameter(description = "ID of the user to retrieve") Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(user.toDTO());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update details of an existing user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data or user already exists", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> updateUser(
            @PathVariable @Parameter(description = "ID of the user to update") Long id,
            @RequestBody @Parameter(description = "Updated user details") UserDTO userDTO,
            HttpServletResponse response) {
        try {
            User userDetails = userDTO.toEntity();
            userDetails.setId(id);
            User updatedUser = userService.updateUser(userDetails);

            User currentUser = userService.getCurrentUser();
            if (currentUser != null && currentUser.getId().equals(id)) {
                Cookie cookie = new Cookie("jwt", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
            }

            return ResponseEntity.ok(updatedUser.toDTO());
        } catch (UserExistedException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Remove a user from the system by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> deleteUser(@PathVariable @Parameter(description = "ID of the user to delete") Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/delete-all")
    @Operation(summary = "Delete multiple users", description = "Remove multiple users from the system by their IDs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users deleted successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> deleteAllUsers(@RequestBody @Parameter(description = "IDs of the users to delete") List<Long> ids) {
        try {
            userService.deleteUsersByIds(ids);
            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("success", "Delete selected users successfully!");
            return ResponseEntity.ok().body(successResponse);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/export")
    @Operation(summary = "Export users to Excel", description = "Export a paginated list of users to an Excel file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Excel file generated successfully", content = @Content(mediaType = "application/vnd.ms-excel")),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Resource> exportToExcel(
            @RequestParam(defaultValue = "0") @Parameter(description = "Page number (default: 0)") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "Page size (default: 10)") int size) {
        try {
            List<User> users = userService.getAllUsers("", page, size).getContent();
            ByteArrayInputStream in = userService.exportToExcel(users);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=users.xlsx");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/import")
    @Operation(summary = "Import users from Excel", description = "Upload an Excel file to import multiple users into the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users imported successfully", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "No file uploaded or invalid file", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Error importing file", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> importExcel(@RequestParam("file") @Parameter(description = "Excel file containing user data") MultipartFile file) {
        if (file.isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Please select a file to upload");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            List<User> users = UserExcelImporter.importUsers(file.getInputStream());
            userService.saveAllFromExcel(users);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Successfully uploaded and imported data");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to import data from file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/profile/{id}")
    @PreAuthorize("hasRole('Admin')")
    @Operation(summary = "Get user profile by ID", description = "Retrieve user profile and recent activities by their ID (Admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(schema = @Schema(implementation = Map.class)))
    })
    public ResponseEntity<?> getUserProfile(@PathVariable @Parameter(description = "ID of the user to retrieve profile") Long id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            List<Activity> activities = activityService.getLogActivityByUserId(id);
            List<Activity> limitedActivities = (activities != null && !activities.isEmpty())
                    ? activities.subList(0, Math.min(10, activities.size()))
                    : List.of();

            Map<String, Object> response = new HashMap<>();
            response.put("user", user.toDTO());
            response.put("activities", limitedActivities);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Retrieve the profile and recent activities of the currently authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<?> getCurrentUserProfile(HttpServletRequest request) {
        String jwt = Optional.ofNullable(request.getCookies())
                .map(Arrays::stream)
                .flatMap(stream -> stream.filter(cookie -> "jwt".equals(cookie.getName())).map(Cookie::getValue).findFirst())
                .orElse(null);

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT token not found");
        }

        try {
            Long userId = jwtTokenUtils.extractUserId(jwt);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT token");
            }

            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            List<Activity> activities = activityService.getLogActivityByUserId(userId);
            List<Activity> limitedActivities = (activities != null && !activities.isEmpty())
                    ? activities.subList(0, Math.min(10, activities.size()))
                    : List.of();

            Map<String, Object> response = new HashMap<>();
            response.put("user", user.toDTO());
            response.put("activities", limitedActivities);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "JWT parsing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @GetMapping("/by-role/{roleId}")
    @Operation(summary = "Get users by role ID", description = "Retrieve all users with the specified role ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "No users found with the specified role")
    })
    public ResponseEntity<?> getUsersByRole(@PathVariable @Parameter(description = "ID of the role") Long roleId) {
        try {
            List<User> users = userService.findByRoleId(roleId);
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No users found for the specified role.");
            }
            return ResponseEntity.ok(users.stream().map(User::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/by-class")
    @Operation(summary = "Get users by class and type", description = "Retrieve users from a class by user type (students, trainers, admins, all)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Class not found or invalid type")
    })
    public ResponseEntity<?> getUsersByClass(
            @RequestParam @Parameter(description = "ID of the class") Long classId,
            @RequestParam @Parameter(description = "User type: students, trainers, admins, or all") String type
    ) {
        try {
            List<User> users = classService.getUsersByClassAndType(classId, type);
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No users found for the specified class and type.");
            }
            return ResponseEntity.ok(users.stream().map(User::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/by-course/{courseId}")
    @Operation(summary = "Get users by course", description = "Retrieve users who are enrolled in the specified course")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully", content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "404", description = "Course not found or no users enrolled")
    })
    public ResponseEntity<?> getUsersByCourse(@PathVariable @Parameter(description = "ID of the course") Long courseId) {
        try {
            List<User> users = courseEnrollmentService.findUsersByCourse(courseId);
            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No users enrolled in this course.");
            }
            return ResponseEntity.ok(users.stream().map(User::toDTO).toList());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

}

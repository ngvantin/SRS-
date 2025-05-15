package com.example.hcm25_cpl_ks_java_01_lms.user;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.example.hcm25_cpl_ks_java_01_lms.chat.Conversation;
import com.example.hcm25_cpl_ks_java_01_lms.chat.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.hcm25_cpl_ks_java_01_lms.activity.Activity;
import com.example.hcm25_cpl_ks_java_01_lms.activity.ActivityService;
import com.example.hcm25_cpl_ks_java_01_lms.common.Constants;
import com.example.hcm25_cpl_ks_java_01_lms.config.security.JwtTokenUtils;
import com.example.hcm25_cpl_ks_java_01_lms.role.Role;
import com.example.hcm25_cpl_ks_java_01_lms.role.RoleService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/users")
@PreAuthorize("@customSecurityService.hasRoleForModule(authentication, 'User')")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final ActivityService activityService ;
    private final JwtTokenUtils jwtTokenUtils;
    private final ConversationService conversationService;

    @Autowired
    public UserController(UserService userService, RoleService roleService, ActivityService activityService, JwtTokenUtils jwtTokenUtils, ConversationService conversationService) {
        this.userService = userService;
        this.roleService = roleService;
        this.activityService = activityService;
        this.jwtTokenUtils = jwtTokenUtils;
        this.conversationService = conversationService;
    }

    @GetMapping
    public String listUsers(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String searchTerm) {
        try{
            Page<User> users = userService.getAllUsers(searchTerm, page, size);
            model.addAttribute("users", users);
            model.addAttribute("searchTerm", searchTerm);
            model.addAttribute("content", "users/list");
            return Constants.LAYOUT;
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("content", "users/list");
            return Constants.LAYOUT;
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        List<Role> roles = roleService.getAllRoles();
        model.addAttribute("roles", roles);
        model.addAttribute("user", new User());
        model.addAttribute("content", "users/create");
        return Constants.LAYOUT;
    }

    @PostMapping
    public String createUser(@ModelAttribute User user, Model model) {
        try {
            userService.createUser(user);
            return "redirect:/users";
        } catch (UserExistedException e) {
            List<Role> roles = roleService.getAllRoles();
            model.addAttribute("roles", roles);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            model.addAttribute("content", "users/create");
            return Constants.LAYOUT;
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        if (user != null) {
            List<Role> roles = roleService.getAllRoles();
            model.addAttribute("roles", roles);
            model.addAttribute("user", user);
            model.addAttribute("content", "users/update");
            return Constants.LAYOUT;
        }
        return "redirect:/users";
    }

    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User userDetails,
                             Model model,
                             HttpServletResponse response) {
        try {
            userDetails.setId(id);
            userService.updateUser(userDetails);

            User currentUser = userService.getCurrentUser();
            if (currentUser != null && currentUser.getId().equals(id)) {
                Cookie cookie = new Cookie("jwt", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);
                return "redirect:/auth/login";
            }

            return "redirect:/users";
        } catch (UserExistedException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", userDetails);
            model.addAttribute("content", "users/update");
            return Constants.LAYOUT;
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("content", "users/update");
            return Constants.LAYOUT;
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportToExcel(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
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
    public String importExcel(@RequestParam("file") MultipartFile file, Model model) {
        model.addAttribute("content", "users/list");
        if (file.isEmpty()) {
            model.addAttribute("error", "Please select a file to upload");
            return Constants.LAYOUT;
        }

        try {
            List<User> users = UserExcelImporter.importUsers(file.getInputStream());
            userService.saveAllFromExcel(users);
            model.addAttribute("success", "Successfully uploaded and imported data");
            return "redirect:/users";
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to import data from file");
            return Constants.LAYOUT;
        }
    }

    @GetMapping("/profile/{id}")
    public String showProfile(@PathVariable Long id, Model model, HttpServletRequest request) {
        // Lấy JWT từ cookie
        String jwt = Optional.ofNullable(request.getCookies())
                .map(Arrays::stream)
                .flatMap(stream -> stream.filter(cookie -> "jwt".equals(cookie.getName())).map(Cookie::getValue).findFirst())
                .orElse(null);

        if (jwt == null) {
            return "redirect:/login";
        }

        Long currentUserId;
        try {
            currentUserId = jwtTokenUtils.extractUserId(jwt);
            if (currentUserId == null) {
                return "redirect:/login";
            }
        } catch (Exception e) {
            System.err.println("JWT parsing error: " + e.getMessage());
            return "redirect:/login";
        }

        // Lấy thông tin người dùng đang xem
        User user = userService.getUserById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Lấy thông tin người dùng đang đăng nhập
        User currentUser = userService.getUserById(currentUserId);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Lấy hoặc tạo Conversation giữa currentUser và user
        Long conversationId = null;
        if (!currentUserId.equals(id)) { // Chỉ lấy conversationId nếu không phải xem profile của chính mình
            Conversation conversation = conversationService.getOrCreateConversationPersonal(currentUser, user);
            conversationId = conversation.getId(); // Lấy ID từ Conversation
        }

        // Lấy danh sách activities
        List<Activity> activities = activityService.getLogActivityByUserId(id);
        List<Activity> limitedActivities = (activities != null && !activities.isEmpty())
                ? activities.subList(0, Math.min(50, activities.size()))
                : List.of();

        System.out.println("Conversation ID between " + currentUserId + " and " + id + ": " + conversationId);
        // Thêm dữ liệu vào model
        model.addAttribute("user", user);
        model.addAttribute("activities", limitedActivities);
        model.addAttribute("currentUser", currentUser); // Thêm currentUser để kiểm tra trong template
        model.addAttribute("conversationId", conversationId); // Thêm conversationId
        model.addAttribute("content", "users/profile");

        return Constants.LAYOUT;
    }

    @GetMapping("/profile")
    public String showMyProfile(Model model, HttpServletRequest request) {
        // Lấy JWT từ cookie
        String jwt = Optional.ofNullable(request.getCookies())
                .map(Arrays::stream)
                .flatMap(stream -> stream.filter(cookie -> "jwt".equals(cookie.getName())).map(Cookie::getValue).findFirst())
                .orElse(null);

        if (jwt == null) {
            return "redirect:/login";
        }

        Long userId;
        try {
            userId = jwtTokenUtils.extractUserId(jwt);
            if (userId == null) {
                return "redirect:/login";
            }
        } catch (Exception e) {
            System.err.println("JWT parsing error: " + e.getMessage());
            return "redirect:/login";
        }

        // Lấy thông tin người dùng đang đăng nhập
        User user = userService.getUserById(userId);
        if (user == null) {
            return "redirect:/login";
        }

        // Lấy danh sách activities
        List<Activity> activities = activityService.getLogActivityByUserId(userId);
        List<Activity> limitedActivities = (activities != null && !activities.isEmpty())
                ? activities.subList(0, Math.min(50, activities.size()))
                : List.of();

        // Không cần conversationId vì đây là profile của chính người dùng
        model.addAttribute("user", user);
        model.addAttribute("activities", limitedActivities);
        model.addAttribute("currentUser", user); // currentUser là chính user
        model.addAttribute("conversationId", null); // Đặt conversationId là null
        model.addAttribute("content", "users/profile");

        return Constants.LAYOUT;
    }
}
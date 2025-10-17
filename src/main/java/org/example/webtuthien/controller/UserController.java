package org.example.webtuthien.controller;

import org.example.webtuthien.model.User;
import org.example.webtuthien.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            System.out.println("Login attempt for email: " + loginRequest.getEmail());
            
            Optional<User> user = userService.getUserByEmail(loginRequest.getEmail());
            
            if (user.isEmpty()) {
                System.out.println("User not found for email: " + loginRequest.getEmail());
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email không tồn tại");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            User foundUser = user.get();
            System.out.println("User found: " + foundUser.getName() + ", Password check: " + foundUser.getPassword().equals(loginRequest.getPassword()));
            
            if (!foundUser.getPassword().equals(loginRequest.getPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Mật khẩu không đúng");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Store user in session
            session.setAttribute("user", foundUser);
            session.setAttribute("userId", foundUser.getId());
            session.setAttribute("userEmail", foundUser.getEmail());

            // Return user info without password
            UserResponse userResponse = new UserResponse();
            userResponse.setId(foundUser.getId());
            userResponse.setName(foundUser.getName());
            userResponse.setEmail(foundUser.getEmail());
            userResponse.setCreatedAt(foundUser.getCreatedAt());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng nhập thành công");
            response.put("user", userResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi đăng nhập: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        try {
            System.out.println("Logout request received");
            System.out.println("Session ID: " + session.getId());
            
            // Clear session
            session.invalidate();
            System.out.println("Session invalidated successfully");
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đăng xuất thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Logout error: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi đăng xuất: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chưa đăng nhập");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Return user info without password
            UserResponse userResponse = new UserResponse();
            userResponse.setId(user.getId());
            userResponse.setName(user.getName());
            userResponse.setEmail(user.getEmail());
            userResponse.setCreatedAt(user.getCreatedAt());

            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi lấy thông tin người dùng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuth(HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            Map<String, Object> response = new HashMap<>();
            response.put("isAuthenticated", user != null);
            
            if (user != null) {
                UserResponse userResponse = new UserResponse();
                userResponse.setId(user.getId());
                userResponse.setName(user.getName());
                userResponse.setEmail(user.getEmail());
                userResponse.setCreatedAt(user.getCreatedAt());
                response.put("user", userResponse);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi kiểm tra đăng nhập: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // Inner classes for request/response
    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class UserResponse {
        private Long id;
        private String name;
        private String email;
        private java.time.OffsetDateTime createdAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public java.time.OffsetDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.OffsetDateTime createdAt) { this.createdAt = createdAt; }
    }

    @PostMapping(value = "/change-password", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, HttpSession session) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Chưa đăng nhập");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            userService.changePassword(user.getId(), request.getCurrentPassword(), request.getNewPassword());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Đổi mật khẩu thành công");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Lỗi đổi mật khẩu: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}

package org.example.webtuthien.controller;

import jakarta.servlet.http.HttpSession;
import org.example.webtuthien.model.User;
import org.example.webtuthien.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public static class LoginForm {
        private String email;
        private String password;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/logout")
    public String handleLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    public static class RegisterForm {
        private String name;
        private String email;
        private String password;
        private String confirmPassword;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }

    public static class ChangePasswordForm {
        private String currentPassword;
        private String newPassword;
        private String confirmNewPassword;
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        public String getConfirmNewPassword() { return confirmNewPassword; }
        public void setConfirmNewPassword(String confirmNewPassword) { this.confirmNewPassword = confirmNewPassword; }
    }

    @PostMapping("/login")
    public String handleLogin(@ModelAttribute LoginForm form, Model model, HttpSession session) {
        var userOpt = userService.getUserByEmail(form.getEmail());
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Email không tồn tại");
            return "login";
        }
        User user = userOpt.get();
        if (!user.getPassword().equals(form.getPassword())) {
            model.addAttribute("error", "Mật khẩu không đúng");
            return "login";
        }
        session.setAttribute("user", user);
        return "redirect:/";
    }

    @PostMapping("/register")
    public String handleRegister(@ModelAttribute RegisterForm form, Model model) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "register";
        }
        try {
            User user = new User();
            user.setName(form.getName());
            user.setEmail(form.getEmail());
            user.setPassword(form.getPassword());
            userService.createUser(user);
            model.addAttribute("success", "Đăng ký thành công. Vui lòng đăng nhập.");
            return "login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }

    @PostMapping("/change-password")
    public String handleChangePassword(@ModelAttribute ChangePasswordForm form, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            model.addAttribute("error", "Chưa đăng nhập");
            return "login";
        }
        if (!form.getNewPassword().equals(form.getConfirmNewPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "change_password";
        }
        try {
            userService.changePassword(user.getId(), form.getCurrentPassword(), form.getNewPassword());
            model.addAttribute("success", "Đổi mật khẩu thành công!");
            return "change_password";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "change_password";
        }
    }
}



package org.example.webtuthien.service;

import org.example.webtuthien.model.Admin;
import org.example.webtuthien.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminAuthService {

    @Autowired
    private AdminRepository adminRepository;

    public Optional<Admin> authenticate(String usernameOrEmail, String password) {
        try {
            System.out.println("AdminAuthService.authenticate called with: " + usernameOrEmail);
            
            // Try to find admin by username first, then by email
            Optional<Admin> adminOpt = adminRepository.findByUsername(usernameOrEmail);
            System.out.println("Found by username: " + adminOpt.isPresent());
            
            if (adminOpt.isEmpty()) {
                adminOpt = adminRepository.findByEmail(usernameOrEmail);
                System.out.println("Found by email: " + adminOpt.isPresent());
            }

            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                System.out.println("Admin found: " + admin.getUsername() + ", active: " + admin.getIsActive());
                
                String storedPassword = admin.getPassword();
                boolean active = Boolean.TRUE.equals(admin.getIsActive());
                
                // Simple password comparison (in production, use proper password hashing)
                if (storedPassword != null && storedPassword.equals(password) && active) {
                    System.out.println("Password match and admin is active");
                    return Optional.of(admin);
                } else {
                    System.out.println("Password mismatch or admin inactive. Password match: " + 
                        (storedPassword != null && storedPassword.equals(password)) + 
                        ", Active: " + active);
                }
            } else {
                System.out.println("No admin found with username/email: " + usernameOrEmail);
            }

            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error in AdminAuthService.authenticate for input '" + usernameOrEmail + "': " + e.getMessage());
            e.printStackTrace();
            // Trả về empty để controller hiển thị lỗi thay vì trả 500
            return Optional.empty();
        }
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    public Admin createAdmin(Admin admin) {
        // Check if username already exists
        if (adminRepository.existsByUsername(admin.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        // Check if email already exists
        if (adminRepository.existsByEmail(admin.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        return adminRepository.save(admin);
    }

    public Admin updateAdmin(Admin admin) {
        if (admin.getId() == null) {
            throw new IllegalArgumentException("Admin ID không được null");
        }

        // Check if admin exists
        Optional<Admin> existingAdmin = adminRepository.findById(admin.getId());
        if (existingAdmin.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy admin");
        }

        // Check if username is being changed and if it already exists
        Admin existing = existingAdmin.get();
        if (!existing.getUsername().equals(admin.getUsername()) && adminRepository.existsByUsername(admin.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }

        // Check if email is being changed and if it already exists
        if (!existing.getEmail().equals(admin.getEmail()) && adminRepository.existsByEmail(admin.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        return adminRepository.save(admin);
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }

    public boolean isAdminActive(Long id) {
        Optional<Admin> admin = adminRepository.findById(id);
        return admin.isPresent() && admin.get().getIsActive();
    }
}

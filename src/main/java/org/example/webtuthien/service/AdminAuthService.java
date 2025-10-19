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
            // Try to find admin by username first, then by email
            Optional<Admin> adminOpt = adminRepository.findByUsername(usernameOrEmail);
            if (adminOpt.isEmpty()) {
                adminOpt = adminRepository.findByEmail(usernameOrEmail);
            }

            if (adminOpt.isPresent()) {
                Admin admin = adminOpt.get();
                // Simple password comparison (in production, use proper password hashing)
                if (admin.getPassword().equals(password) && admin.getIsActive()) {
                    return Optional.of(admin);
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Error in AdminAuthService.authenticate: " + e.getMessage());
            e.printStackTrace();
            throw e;
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

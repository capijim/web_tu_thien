package org.example.webtuthien.repository;

import org.example.webtuthien.model.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class AdminRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Admin> adminRowMapper = new RowMapper<Admin>() {
        @Override
        public Admin mapRow(@org.springframework.lang.NonNull ResultSet rs, int rowNum) throws SQLException {
            Admin admin = new Admin();
            admin.setId(rs.getLong("id"));
            admin.setUsername(rs.getString("username"));
            admin.setEmail(rs.getString("email"));
            admin.setPassword(rs.getString("password"));
            admin.setFullName(rs.getString("full_name"));
            admin.setIsActive(rs.getBoolean("is_active"));
            admin.setCreatedAt(rs.getObject("created_at", java.time.OffsetDateTime.class));
            admin.setUpdatedAt(rs.getObject("updated_at", java.time.OffsetDateTime.class));
            return admin;
        }
    };

    public List<Admin> findAll() {
        String sql = "SELECT * FROM admins WHERE is_active = true ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, adminRowMapper);
    }

    public Optional<Admin> findById(Long id) {
        String sql = "SELECT * FROM admins WHERE id = ? AND is_active = true";
        List<Admin> admins = jdbcTemplate.query(sql, adminRowMapper, id);
        return admins.isEmpty() ? Optional.empty() : Optional.of(admins.get(0));
    }

    public Optional<Admin> findByUsername(String username) {
        String sql = "SELECT * FROM admins WHERE username = ? AND is_active = true";
        List<Admin> admins = jdbcTemplate.query(sql, adminRowMapper, username);
        return admins.isEmpty() ? Optional.empty() : Optional.of(admins.get(0));
    }

    public Optional<Admin> findByEmail(String email) {
        String sql = "SELECT * FROM admins WHERE email = ? AND is_active = true";
        List<Admin> admins = jdbcTemplate.query(sql, adminRowMapper, email);
        return admins.isEmpty() ? Optional.empty() : Optional.of(admins.get(0));
    }

    public Admin save(Admin admin) {
        if (admin.getId() == null) {
            // Insert new admin
            String sql = "INSERT INTO admins (username, email, password, full_name, is_active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, NOW(), NOW())";
            jdbcTemplate.update(sql, admin.getUsername(), admin.getEmail(), admin.getPassword(), admin.getFullName(), admin.getIsActive());
            
            // Get the generated ID
            String getIdSql = "SELECT LAST_INSERT_ID()";
            Long id = jdbcTemplate.queryForObject(getIdSql, Long.class);
            admin.setId(id);
        } else {
            // Update existing admin
            String sql = "UPDATE admins SET username = ?, email = ?, password = ?, full_name = ?, is_active = ?, updated_at = NOW() WHERE id = ?";
            jdbcTemplate.update(sql, admin.getUsername(), admin.getEmail(), admin.getPassword(), admin.getFullName(), admin.getIsActive(), admin.getId());
        }
        return admin;
    }

    public void deleteById(Long id) {
        String sql = "UPDATE admins SET is_active = false, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM admins WHERE username = ? AND is_active = true";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username);
        return count != null && count > 0;
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM admins WHERE email = ? AND is_active = true";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
}

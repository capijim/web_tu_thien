package org.example.webtuthien.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        @NonNull
        public User mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setName(rs.getString("name"));
            user.setEmail(rs.getString("email"));
            user.setPassword(rs.getString("password"));
            Timestamp timestamp = rs.getTimestamp("created_at");
            user.setCreatedAt(timestamp != null ? timestamp.toLocalDateTime().atOffset(java.time.ZoneOffset.UTC) : null);
            return user;
        }
    };

    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> findByEmail(String email) {
        try {
            String sql = "SELECT * FROM users WHERE email = ?";
            List<User> users = jdbcTemplate.query(sql, userRowMapper, email);
            return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        } catch (Exception e) {
            System.err.println("Error in findByEmail: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public User save(User user) {
        if (user.getId() == null) {
            // Insert new user
            String sql = "INSERT INTO users (name, email, password, created_at) VALUES (?, ?, ?, NOW())";
            jdbcTemplate.update(sql, user.getName(), user.getEmail(), user.getPassword());
            
            // Get the generated ID
            String selectSql = "SELECT * FROM users WHERE email = ? ORDER BY created_at DESC LIMIT 1";
            List<User> users = jdbcTemplate.query(selectSql, userRowMapper, user.getEmail());
            return users.get(0);
        } else {
            // Update existing user
            String sql = "UPDATE users SET name = ?, email = ?, password = ? WHERE id = ?";
            jdbcTemplate.update(sql, user.getName(), user.getEmail(), user.getPassword(), user.getId());
            return user;
        }
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }
}

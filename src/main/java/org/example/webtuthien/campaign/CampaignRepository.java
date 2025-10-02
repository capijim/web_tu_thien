package org.example.webtuthien.campaign;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.math.BigDecimal;

@Repository
public class CampaignRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final JdbcTemplate coreJdbc;
    private final SimpleJdbcInsert insertCampaign;

    public CampaignRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.coreJdbc = jdbc.getJdbcTemplate();
        this.insertCampaign = new SimpleJdbcInsert(this.coreJdbc)
                .withTableName("campaigns")
                .usingGeneratedKeyColumns("id")
                .usingColumns("user_id", "title", "description", "target_amount", 
                            "current_amount", "category", "image_url", "status", "end_date");
    }

    private static final RowMapper<Campaign> ROW_MAPPER = new RowMapper<>() {
        @Override
        public Campaign mapRow(ResultSet rs, int rowNum) throws SQLException {
            Campaign campaign = new Campaign();
            campaign.setId(rs.getLong("id"));
            campaign.setUserId(rs.getLong("user_id"));
            campaign.setTitle(rs.getString("title"));
            campaign.setDescription(rs.getString("description"));
            campaign.setTargetAmount(rs.getBigDecimal("target_amount"));
            campaign.setCurrentAmount(rs.getBigDecimal("current_amount"));
            campaign.setCategory(rs.getString("category"));
            campaign.setImageUrl(rs.getString("image_url"));
            campaign.setStatus(rs.getString("status"));
            
            Timestamp endDateTs = rs.getTimestamp("end_date");
            if (endDateTs != null) {
                campaign.setEndDate(OffsetDateTime.ofInstant(endDateTs.toInstant(), ZoneOffset.UTC));
            }
            
            Timestamp createdAtTs = rs.getTimestamp("created_at");
            if (createdAtTs != null) {
                campaign.setCreatedAt(OffsetDateTime.ofInstant(createdAtTs.toInstant(), ZoneOffset.UTC));
            }
            
            return campaign;
        }
    };

    public List<Campaign> findAll() {
        String sql = "select id, user_id, title, description, target_amount, current_amount, " +
                    "category, image_url, status, end_date, created_at from campaigns " +
                    "order by created_at desc";
        return jdbc.query(sql, ROW_MAPPER);
    }

    public List<Campaign> findByUserId(Long userId) {
        String sql = "select id, user_id, title, description, target_amount, current_amount, " +
                    "category, image_url, status, end_date, created_at from campaigns " +
                    "where user_id = :userId order by created_at desc";
        return jdbc.query(sql, new MapSqlParameterSource("userId", userId), ROW_MAPPER);
    }

    public List<Campaign> findByCategory(String category) {
        String sql = "select id, user_id, title, description, target_amount, current_amount, " +
                    "category, image_url, status, end_date, created_at from campaigns " +
                    "where category = :category and status = 'active' order by created_at desc";
        return jdbc.query(sql, new MapSqlParameterSource("category", category), ROW_MAPPER);
    }

    public List<Campaign> findActive() {
        String sql = "select id, user_id, title, description, target_amount, current_amount, " +
                    "category, image_url, status, end_date, created_at from campaigns " +
                    "where status = 'active' order by created_at desc";
        return jdbc.query(sql, ROW_MAPPER);
    }

    public Optional<Campaign> findById(Long id) {
        String sql = "select id, user_id, title, description, target_amount, current_amount, " +
                    "category, image_url, status, end_date, created_at from campaigns " +
                    "where id = :id";
        List<Campaign> list = jdbc.query(sql, new MapSqlParameterSource("id", id), ROW_MAPPER);
        return list.stream().findFirst();
    }

    public Campaign insert(Campaign campaign) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", campaign.getUserId());
        params.put("title", campaign.getTitle());
        params.put("description", campaign.getDescription());
        params.put("target_amount", campaign.getTargetAmount());
        params.put("current_amount", campaign.getCurrentAmount());
        params.put("category", campaign.getCategory());
        params.put("image_url", campaign.getImageUrl());
        params.put("status", campaign.getStatus());
        params.put("end_date", campaign.getEndDate());

        Number key = insertCampaign.executeAndReturnKey(params);
        Long id = key.longValue();
        return findById(id).orElseThrow(() -> new IllegalStateException("Inserted campaign not found"));
    }

    public void updateCurrentAmount(Long id, BigDecimal newAmount) {
        String sql = "update campaigns set current_amount = :amount where id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("amount", newAmount);
        params.addValue("id", id);
        jdbc.update(sql, params);
    }

    public void updateStatus(Long id, String status) {
        String sql = "update campaigns set status = :status where id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("status", status);
        params.addValue("id", id);
        jdbc.update(sql, params);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM campaigns WHERE id = :id";
        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }
}

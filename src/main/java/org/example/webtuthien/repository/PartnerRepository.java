package org.example.webtuthien.repository;

import org.example.webtuthien.model.Partner;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Repository
public class PartnerRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final SimpleJdbcInsert insertPartner;

    public PartnerRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.insertPartner = new SimpleJdbcInsert(jdbc.getJdbcTemplate())
                .withTableName("partners")
                .usingGeneratedKeyColumns("id")
                .usingColumns("name", "email", "phone", "address");
    }

    private static final RowMapper<Partner> ROW_MAPPER = new RowMapper<>() {
        @Override
        public Partner mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            Partner p = new Partner();
            p.setId(rs.getLong("id"));
            p.setName(rs.getString("name"));
            p.setEmail(rs.getString("email"));
            p.setPhone(rs.getString("phone"));
            p.setAddress(rs.getString("address"));
            Timestamp created = rs.getTimestamp("created_at");
            if (created != null) {
                p.setCreatedAt(OffsetDateTime.ofInstant(created.toInstant(), ZoneOffset.UTC));
            }
            return p;
        }
    };

    public List<Partner> findAll() {
        String sql = "select id, name, email, phone, address, created_at from partners order by created_at desc";
        return jdbc.query(sql, ROW_MAPPER);
    }

    public Optional<Partner> findById(Long id) {
        String sql = "select id, name, email, phone, address, created_at from partners where id = :id";
        List<Partner> list = jdbc.query(sql, new MapSqlParameterSource("id", id), ROW_MAPPER);
        return list.stream().findFirst();
    }

    public Partner insert(Partner partner) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", partner.getName());
        params.put("email", partner.getEmail());
        params.put("phone", partner.getPhone());
        params.put("address", partner.getAddress());
        Number key = insertPartner.executeAndReturnKey(params);
        return findById(key.longValue()).orElseThrow();
    }

    public void deleteById(Long id) {
        String sql = "delete from partners where id = :id";
        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }
}



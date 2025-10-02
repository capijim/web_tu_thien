package org.example.webtuthien.donation;

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

@Repository
public class DonationRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final JdbcTemplate coreJdbc;
    private final SimpleJdbcInsert insertDonation;

    public DonationRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.coreJdbc = jdbc.getJdbcTemplate();
        this.insertDonation = new SimpleJdbcInsert(this.coreJdbc)
                .withTableName("donations")
                .usingGeneratedKeyColumns("id")
                .usingColumns("donor_name", "amount", "message");
    }

    private static final RowMapper<Donation> ROW_MAPPER = new RowMapper<>() {
        @Override
        public Donation mapRow(ResultSet rs, int rowNum) throws SQLException {
            Donation d = new Donation();
            d.setId(rs.getLong("id"));
            d.setDonorName(rs.getString("donor_name"));
            d.setAmount(rs.getBigDecimal("amount"));
            d.setMessage(rs.getString("message"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) {
                d.setCreatedAt(OffsetDateTime.ofInstant(ts.toInstant(), ZoneOffset.UTC));
            }
            return d;
        }
    };

    public List<Donation> findAll() {
        String sql = "select id, donor_name, amount, message, created_at from donations order by created_at desc";
        return jdbc.query(sql, ROW_MAPPER);
    }

    public Optional<Donation> findById(Long id) {
        String sql = "select id, donor_name, amount, message, created_at from donations where id = :id";
        List<Donation> list = jdbc.query(sql, new MapSqlParameterSource("id", id), ROW_MAPPER);
        return list.stream().findFirst();
    }

    public Donation insert(Donation donation) {
        Map<String, Object> params = new HashMap<>();
        params.put("donor_name", donation.getDonorName());
        params.put("amount", donation.getAmount());
        params.put("message", donation.getMessage());

        Number key = insertDonation.executeAndReturnKey(params);
        Long id = key.longValue();
        return findById(id).orElseThrow(() -> new IllegalStateException("Inserted donation not found"));
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM donations WHERE id = :id";
        jdbc.update(sql, new MapSqlParameterSource("id", id));
    }
}



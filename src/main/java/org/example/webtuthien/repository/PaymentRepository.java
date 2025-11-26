package org.example.webtuthien.repository;

import org.example.webtuthien.model.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    private final RowMapper<Payment> rowMapper = (rs, rowNum) -> {
        Payment payment = new Payment();
        payment.setId(rs.getLong("id"));
        payment.setDonationId(rs.getLong("donation_id"));
        payment.setVnpayTransactionId(rs.getString("vnpay_transaction_id"));
        payment.setVnpayResponseCode(rs.getString("vnpay_response_code"));
        payment.setVnpayTxnRef(rs.getString("vnpay_txn_ref"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setBankCode(rs.getString("bank_code"));
        payment.setPaymentStatus(rs.getString("payment_status"));
        
        if (rs.getTimestamp("created_at") != null) {
            payment.setCreatedAt(rs.getTimestamp("created_at").toInstant().atOffset(java.time.ZoneOffset.UTC));
        }
        if (rs.getTimestamp("updated_at") != null) {
            payment.setUpdatedAt(rs.getTimestamp("updated_at").toInstant().atOffset(java.time.ZoneOffset.UTC));
        }
        return payment;
    };
    
    public Payment save(Payment payment) {
        String sql = "INSERT INTO payments (donation_id, vnpay_txn_ref, amount, payment_status, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, NOW(), NOW())";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(1, payment.getDonationId());
            ps.setString(2, payment.getVnpayTxnRef());
            ps.setBigDecimal(3, payment.getAmount());
            ps.setString(4, payment.getPaymentStatus());
            return ps;
        }, keyHolder);
        
        if (keyHolder.getKey() != null) {
            payment.setId(keyHolder.getKey().longValue());
        }
        return payment;
    }
    
    public Optional<Payment> findByVnpayTxnRef(String vnpayTxnRef) {
        String sql = "SELECT * FROM payments WHERE vnpay_txn_ref = ?";
        List<Payment> payments = jdbcTemplate.query(sql, rowMapper, vnpayTxnRef);
        return payments.isEmpty() ? Optional.empty() : Optional.of(payments.get(0));
    }
    
    public void updatePaymentStatus(Long id, String status, String transactionId, String responseCode) {
        String sql = "UPDATE payments SET payment_status = ?, vnpay_transaction_id = ?, " +
                    "vnpay_response_code = ?, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, status, transactionId, responseCode, id);
    }
    
    public void updateBankCode(Long id, String bankCode) {
        String sql = "UPDATE payments SET bank_code = ?, updated_at = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, bankCode, id);
    }
    
    public List<Payment> findAll() {
        String sql = "SELECT * FROM payments ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }
}

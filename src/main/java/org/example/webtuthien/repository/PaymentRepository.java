package org.example.webtuthien.repository;

import org.example.webtuthien.model.Payment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PaymentRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public PaymentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    private final RowMapper<Payment> paymentRowMapper = (rs, rowNum) -> {
        Payment payment = new Payment();
        payment.setId(rs.getLong("id"));
        payment.setDonationId(rs.getLong("donation_id"));
        payment.setVnpayTxnRef(rs.getString("vnpay_txn_ref"));
        payment.setVnpayTransactionId(rs.getString("vnpay_transaction_id"));
        payment.setVnpayResponseCode(rs.getString("vnpay_response_code"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentStatus(rs.getString("payment_status"));
        payment.setCampaignId(rs.getLong("campaign_id"));
        return payment;
    };
    
    public Payment findByVnpayTxnRef(String vnpayTxnRef) {
        String sql = "SELECT * FROM payments WHERE vnpay_txn_ref = ?";
        try {
            return jdbcTemplate.queryForObject(sql, paymentRowMapper, vnpayTxnRef);
        } catch (Exception e) {
            return null;
        }
    }
    
    public void save(Payment payment) {
        String sql = "INSERT INTO payments (donation_id, vnpay_txn_ref, amount, payment_status, campaign_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, 
            payment.getDonationId(),
            payment.getVnpayTxnRef(),
            payment.getAmount(),
            payment.getPaymentStatus(),
            payment.getCampaignId()
        );
    }
}

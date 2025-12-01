package org.example.webtuthien.service;

import org.example.webtuthien.config.VNPayConfig;
import org.example.webtuthien.model.Donation;
import org.example.webtuthien.model.Payment;
import org.example.webtuthien.repository.PaymentRepository;
import org.example.webtuthien.util.VNPayUtil;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {
    
    private final VNPayConfig vnPayConfig;
    private final PaymentRepository paymentRepository;
    
    public VNPayService(VNPayConfig vnPayConfig, PaymentRepository paymentRepository) {
        this.vnPayConfig = vnPayConfig;
        this.paymentRepository = paymentRepository;
    }
    
    public String createPaymentUrl(Donation donation, HttpServletRequest request) 
            throws UnsupportedEncodingException {
        
        // Kiểm tra donation có ID chưa
        if (donation.getId() == null) {
            throw new IllegalArgumentException("Donation ID is null. Donation must be saved before creating payment URL");
        }
        
        System.out.println("Creating payment for donation ID: " + donation.getId());
        System.out.println("Donation amount: " + donation.getAmount());
        
        // Tạo payment record với status PENDING
        Payment payment = new Payment();
        payment.setDonationId(donation.getId());
        payment.setAmount(donation.getAmount());
        payment.setPaymentStatus("PENDING");
        
        // Tạo mã giao dịch duy nhất
        String vnpTxnRef = VNPayUtil.getRandomNumber(8);
        payment.setVnpayTxnRef(vnpTxnRef);
        
        System.out.println("Payment details - donation_id: " + payment.getDonationId() + 
                          ", vnpay_txn_ref: " + payment.getVnpayTxnRef() + 
                          ", amount: " + payment.getAmount());
        
        // Lưu payment
        paymentRepository.save(payment);
        
        // Tạo các tham số cho VNPay
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(donation.getAmount().multiply(new java.math.BigDecimal("100")).longValue()));
        vnpParams.put("vnp_CurrCode", "VND");
        
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", "Quyen gop cho chien dich: " + donation.getCampaignId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        
        // Sử dụng múi giờ Việt Nam
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar cld = Calendar.getInstance(timeZone);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(timeZone);
        
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        
        cld.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);
        
        // Sắp xếp các tham số
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        
        String queryUrl = query.toString();
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        
        return vnPayConfig.getVnpayUrl() + "?" + queryUrl;
    }
    
    public Map<String, Object> handlePaymentReturn(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }
        
        String vnpSecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        
        String signValue = VNPayUtil.hashAllFields(fields);
        String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), signValue);
        
        if (calculatedHash.equals(vnpSecureHash)) {
            String vnpTxnRef = request.getParameter("vnp_TxnRef");
            String vnpResponseCode = request.getParameter("vnp_ResponseCode");
            String vnpTransactionNo = request.getParameter("vnp_TransactionNo");
            String vnpBankCode = request.getParameter("vnp_BankCode");
            
            Optional<Payment> paymentOpt = paymentRepository.findByVnpayTxnRef(vnpTxnRef);
            
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                
                if ("00".equals(vnpResponseCode)) {
                    paymentRepository.updatePaymentStatus(payment.getId(), "SUCCESS", 
                        vnpTransactionNo, vnpResponseCode);
                    paymentRepository.updateBankCode(payment.getId(), vnpBankCode);
                    
                    result.put("success", true);
                    result.put("message", "Thanh toán thành công");
                    result.put("donationId", payment.getDonationId());
                } else {
                    paymentRepository.updatePaymentStatus(payment.getId(), "FAILED", 
                        vnpTransactionNo, vnpResponseCode);
                    
                    result.put("success", false);
                    result.put("message", "Thanh toán thất bại. Mã lỗi: " + vnpResponseCode);
                }
            } else {
                result.put("success", false);
                result.put("message", "Không tìm thấy giao dịch");
            }
        } else {
            result.put("success", false);
            result.put("message", "Chữ ký không hợp lệ");
        }
        
        return result;
    }
}

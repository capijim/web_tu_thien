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
        
        if (donation.getId() == null) {
            throw new IllegalArgumentException("Donation ID is null. Donation must be saved before creating payment URL");
        }
        
        System.out.println("=== Creating VNPay Payment ===");
        System.out.println("Donation ID: " + donation.getId());
        System.out.println("Campaign ID: " + donation.getCampaignId());
        System.out.println("Amount: " + donation.getAmount());
        
        String vnpTxnRef = VNPayUtil.getRandomNumber(8);
        
        // Tạo các tham số cho VNPay
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(donation.getAmount().multiply(new java.math.BigDecimal("100")).longValue()));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", "Ung ho chien dich " + donation.getCampaignId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar cld = Calendar.getInstance(timeZone);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(timeZone);
        
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        
        cld.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);
        
        // QUAN TRỌNG: Tạo hash data TRƯỚC KHI encode URL
        String hashData = VNPayUtil.buildQueryString(vnpParams);
        System.out.println("Hash Data (before hash): " + hashData);
        
        // Tạo secure hash
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        System.out.println("Secure Hash: " + vnpSecureHash);
        
        // Tạo query URL với encoding
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder query = new StringBuilder();
        
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = vnpParams.get(fieldName);
            
            if (fieldValue != null && fieldValue.length() > 0) {
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                
                if (i < fieldNames.size() - 1) {
                    query.append('&');
                }
            }
        }
        
        // Thêm secure hash vào cuối
        String queryUrl = query.toString() + "&vnp_SecureHash=" + vnpSecureHash;
        
        String fullUrl = vnPayConfig.getVnpayUrl() + "?" + queryUrl;
        System.out.println("VNPay URL created: " + fullUrl);
        
        return fullUrl;
    }
    
    public Map<String, Object> handlePaymentReturn(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        // Lấy tất cả parameters
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }
        
        System.out.println("=== VNPay Return Callback ===");
        System.out.println("All params: " + fields);
        
        String vnpSecureHash = fields.get("vnp_SecureHash");
        
        // Loại bỏ các field không dùng để tạo hash
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        
        // Tạo hash data từ params
        String hashData = VNPayUtil.buildQueryString(fields);
        System.out.println("Hash Data from return: " + hashData);
        
        // Tính toán hash
        String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        System.out.println("Received Hash: " + vnpSecureHash);
        System.out.println("Calculated Hash: " + calculatedHash);
        System.out.println("Hash valid: " + calculatedHash.equals(vnpSecureHash));
        
        if (calculatedHash.equals(vnpSecureHash)) {
            String vnpResponseCode = request.getParameter("vnp_ResponseCode");
            
            if ("00".equals(vnpResponseCode)) {
                result.put("success", true);
                result.put("message", "Thanh toán thành công!");
                System.out.println("Payment successful");
            } else {
                result.put("success", false);
                result.put("message", "Thanh toán thất bại. Mã lỗi: " + vnpResponseCode);
                System.out.println("Payment failed with code: " + vnpResponseCode);
            }
        } else {
            result.put("success", false);
            result.put("message", "Chữ ký không hợp lệ");
            System.out.println("Invalid signature!");
        }
        
        return result;
    }
}

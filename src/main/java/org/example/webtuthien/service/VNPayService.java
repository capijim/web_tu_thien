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
        
        System.out.println("=== Creating VNPay Payment ===");
        System.out.println("Donation ID: " + donation.getId());
        System.out.println("Campaign ID: " + donation.getCampaignId());
        System.out.println("Amount: " + donation.getAmount());
        System.out.println("Donor Name: " + donation.getDonorName());
        
        String vnpTxnRef = VNPayUtil.getRandomNumber(8);
        System.out.println("Payment TxnRef: " + vnpTxnRef);
        System.out.println("SKIPPING database save - testing VNPay only");
        
        // Get return URL
        String returnUrl = vnPayConfig.getReturnUrl();
        System.out.println("=== VNPay Configuration ===");
        System.out.println("Return URL: " + returnUrl);
        System.out.println("Base URL: " + vnPayConfig.getBaseUrl());
        System.out.println("TMN Code: " + vnPayConfig.getTmnCode());
        
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
        vnpParams.put("vnp_ReturnUrl", returnUrl);
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
        
        System.out.println("=== Final VNPay URL ===");
        System.out.println(vnPayConfig.getVnpayUrl() + "?" + queryUrl);
        
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
        
        System.out.println("=== VNPay Return Callback ===");
        System.out.println("Response Code: " + request.getParameter("vnp_ResponseCode"));
        System.out.println("TxnRef: " + request.getParameter("vnp_TxnRef"));
        System.out.println("TransactionNo: " + request.getParameter("vnp_TransactionNo"));
        System.out.println("Hash valid: " + calculatedHash.equals(vnpSecureHash));
        
        if (calculatedHash.equals(vnpSecureHash)) {
            String vnpResponseCode = request.getParameter("vnp_ResponseCode");
            
            // TẠM THỜI BỎ QUA CẬP NHẬT DB
            if ("00".equals(vnpResponseCode)) {
                result.put("success", true);
                result.put("message", "Thanh toán thành công! (Test mode - chưa lưu DB)");
                System.out.println("Payment successful - DB save skipped for testing");
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

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
        
        // Tạo các tham số - TreeMap tự động sort
        Map<String, String> vnpParams = new TreeMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(donation.getAmount().multiply(new java.math.BigDecimal("100")).longValue()));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", "DonateC" + donation.getCampaignId());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", VNPayUtil.getIpAddress(request));
        
        // Tạo thời gian
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar cld = Calendar.getInstance(timeZone);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        formatter.setTimeZone(timeZone);
        
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        
        cld.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);
        
        // Tạo hash data và query string - GIỐNG NHAU HOÀN TOÀN
        StringBuilder queryData = new StringBuilder();
        boolean isFirst = true;
        
        for (Map.Entry<String, String> entry : vnpParams.entrySet()) {
            if (!isFirst) {
                queryData.append('&');
            }
            queryData.append(entry.getKey()).append('=').append(entry.getValue());
            isFirst = false;
        }
        
        String hashData = queryData.toString();
        
        System.out.println("=== VNPay Debug ===");
        System.out.println("Hash Data: " + hashData);
        System.out.println("Hash Secret length: " + vnPayConfig.getHashSecret().length());
        
        // Tạo secure hash
        String vnpSecureHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        System.out.println("Secure Hash: " + vnpSecureHash);
        
        // Query URL = Hash Data + SecureHash (KHÔNG ENCODE GÌ CẢ)
        String queryUrl = hashData + "&vnp_SecureHash=" + vnpSecureHash;
        
        String fullUrl = vnPayConfig.getVnpayUrl() + "?" + queryUrl;
        
        System.out.println("=== Final URL ===");
        System.out.println("Length: " + fullUrl.length());
        System.out.println("URL: " + fullUrl);
        
        return fullUrl;
    }
    
    public Map<String, Object> handlePaymentReturn(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
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
            if (vnpSecureHash == null) {
                result.put("success", false);
                result.put("message", "Thiếu chữ ký bảo mật");
                return result;
            }
            
            // Loại bỏ các field không dùng để tạo hash
            fields.remove("vnp_SecureHashType");
            fields.remove("vnp_SecureHash");
            
            // Tạo hash data từ params (đã được decode tự động bởi servlet)
            String hashData = VNPayUtil.buildQueryString(fields);
            System.out.println("Hash Data from return: " + hashData);
            
            // Tính toán hash
            String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
            System.out.println("Received Hash: " + vnpSecureHash);
            System.out.println("Calculated Hash: " + calculatedHash);
            System.out.println("Hash valid: " + calculatedHash.equals(vnpSecureHash));
            
            if (calculatedHash.equals(vnpSecureHash)) {
                String vnpResponseCode = request.getParameter("vnp_ResponseCode");
                String vnpTxnRef = request.getParameter("vnp_TxnRef");
                
                if ("00".equals(vnpResponseCode)) {
                    result.put("success", true);
                    result.put("message", "Thanh toán thành công! Cảm ơn bạn đã đóng góp cho chiến dịch.");
                    System.out.println("Payment successful - TxnRef: " + vnpTxnRef);
                    
                    // Lấy campaignId từ OrderInfo
                    String orderInfo = request.getParameter("vnp_OrderInfo");
                    if (orderInfo != null && orderInfo.startsWith("DonateC")) {
                        try {
                            Long campaignId = Long.parseLong(orderInfo.substring(7));
                            result.put("campaignId", campaignId);
                        } catch (Exception e) {
                            System.out.println("Could not parse campaignId from OrderInfo: " + e.getMessage());
                        }
                    }
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
        } catch (Exception e) {
            System.err.println("Error processing VNPay return: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Lỗi xử lý kết quả thanh toán: " + e.getMessage());
        }
        
        return result;
    }
}

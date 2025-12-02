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
        System.out.println("Mock Mode: " + vnPayConfig.isMockMode());
        System.out.println("Donation ID: " + donation.getId());
        System.out.println("Amount: " + donation.getAmount());
        
        String vnpTxnRef = VNPayUtil.getRandomNumber(8);
        
        // Nếu đang ở Mock Mode, redirect đến trang mock thay vì VNPay
        if (vnPayConfig.isMockMode()) {
            System.out.println("🧪 Mock Mode: Redirecting to mock payment page");
            return vnPayConfig.getBaseUrl() + "/vnpay/mock?txnRef=" + vnpTxnRef + 
                   "&amount=" + donation.getAmount() + 
                   "&orderInfo=" + URLEncoder.encode("Donation_" + donation.getCampaignId(), StandardCharsets.UTF_8.toString());
        }
        
        // Get return URL
        String returnUrl = vnPayConfig.getReturnUrl();
        System.out.println("Return URL: " + returnUrl);
        
        // Tạo các tham số cho VNPay
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", vnPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(donation.getAmount().multiply(new java.math.BigDecimal("100")).longValue()));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", "Donation_" + donation.getCampaignId() + "_" + donation.getId());
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
        
        return vnPayConfig.getVnpayUrl() + "?" + queryUrl;
    }
    
    public Map<String, Object> handlePaymentReturn(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        
        System.out.println("=== VNPay Return Callback ===");
        
        String vnpResponseCode = request.getParameter("vnp_ResponseCode");
        String vnpSecureHash = request.getParameter("vnp_SecureHash");
        
        System.out.println("Response Code: " + vnpResponseCode);
        System.out.println("Secure Hash: " + (vnpSecureHash != null ? vnpSecureHash.substring(0, 10) + "..." : "null"));
        
        // Nếu là Mock payment (hash bắt đầu bằng MOCK_)
        if (vnpSecureHash != null && vnpSecureHash.startsWith("MOCK_")) {
            System.out.println("🧪 Mock Mode: Simulated payment detected");
            
            if ("00".equals(vnpResponseCode)) {
                result.put("success", true);
                result.put("message", "Thanh toán thành công! (Mock Mode - Railway Demo)");
            } else {
                result.put("success", false);
                result.put("message", "Giao dịch bị hủy hoặc thất bại");
            }
            return result;
        }
        
        // ...existing real VNPay validation code...
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }
        
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");
        
        String signValue = VNPayUtil.hashAllFields(fields);
        String calculatedHash = VNPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), signValue);
        
        if (calculatedHash.equals(vnpSecureHash)) {
            if ("00".equals(vnpResponseCode)) {
                result.put("success", true);
                result.put("message", "Thanh toán thành công!");
            } else {
                result.put("success", false);
                result.put("message", "Thanh toán thất bại. Mã lỗi: " + vnpResponseCode);
            }
        } else {
            result.put("success", false);
            result.put("message", "Chữ ký không hợp lệ");
        }
        
        return result;
    }
}

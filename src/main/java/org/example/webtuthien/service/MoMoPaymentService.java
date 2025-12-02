package org.example.webtuthien.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class MoMoPaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(MoMoPaymentService.class);
    
    @Value("${momo.partner-code:MOMO}")
    private String partnerCode;
    
    @Value("${momo.access-key:F8BBA842ECF85}")
    private String accessKey;
    
    @Value("${momo.secret-key:K951B6PE1waDMi640xX08PD3vg6EkVlz}")
    private String secretKey;
    
    @Value("${momo.endpoint:https://test-payment.momo.vn/v2/gateway/api/create}")
    private String endpoint;
    
    @Value("${momo.redirect-url:https://webtuthien-production.up.railway.app/payment/momo/callback}")
    private String redirectUrl;
    
    @Value("${momo.ipn-url:https://webtuthien-production.up.railway.app/payment/momo/ipn}")
    private String ipnUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> createPayment(String orderId, Long amount, String orderInfo, String extraData) {
        try {
            String requestId = orderId;
            String requestType = "captureWallet";
            
            // QUAN TRỌNG: Sắp xếp params theo alphabet để tạo signature
            String rawSignature = String.format(
                "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                accessKey, amount, extraData, ipnUrl, orderId, orderInfo, partnerCode, redirectUrl, requestId, requestType
            );
            
            logger.info("Raw signature: {}", rawSignature);
            
            String signature = hmacSHA256(rawSignature, secretKey);
            logger.info("Signature: {}", signature);
            
            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("accessKey", accessKey);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", redirectUrl);
            requestBody.put("ipnUrl", ipnUrl);
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", requestType);
            requestBody.put("signature", signature);
            requestBody.put("lang", "vi");
            
            logger.info("Sending request to MoMo: {}", endpoint);
            logger.info("Request body: {}", requestBody);
            
            // Send request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            Map<String, Object> response = restTemplate.postForObject(endpoint, entity, Map.class);
            logger.info("MoMo response: {}", response);
            
            return response;
        } catch (Exception e) {
            logger.error("Error creating MoMo payment", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("resultCode", -1);
            errorResponse.put("message", "Lỗi kết nối MoMo: " + e.getMessage());
            return errorResponse;
        }
    }
    
    public Map<String, Object> createATMPayment(String orderId, Long amount, String orderInfo, String extraData) {
        try {
            String requestId = orderId;
            String requestType = "payWithATM"; // Khác với QR (captureWallet)
            
            // Tạo signature cho ATM payment
            String rawSignature = String.format(
                "accessKey=%s&amount=%d&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                accessKey, amount, extraData, ipnUrl, orderId, orderInfo, partnerCode, redirectUrl, requestId, requestType
            );
            
            logger.info("ATM Raw signature: {}", rawSignature);
            
            String signature = hmacSHA256(rawSignature, secretKey);
            logger.info("ATM Signature: {}", signature);
            
            // Build request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("accessKey", accessKey);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", redirectUrl);
            requestBody.put("ipnUrl", ipnUrl);
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", requestType); // payWithATM
            requestBody.put("signature", signature);
            requestBody.put("lang", "vi");
            
            logger.info("Sending ATM payment request to MoMo: {}", endpoint);
            logger.info("Request body: {}", requestBody);
            
            // Send request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            Map<String, Object> response = restTemplate.postForObject(endpoint, entity, Map.class);
            logger.info("MoMo ATM response: {}", response);
            
            return response;
        } catch (Exception e) {
            logger.error("Error creating MoMo ATM payment", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("resultCode", -1);
            errorResponse.put("message", "Lỗi kết nối MoMo ATM: " + e.getMessage());
            return errorResponse;
        }
    }
    
    public boolean verifySignature(Map<String, String> params) {
        try {
            String receivedSignature = params.get("signature");
            if (receivedSignature == null) {
                logger.error("Signature is null in callback params");
                return false;
            }
            
            logger.info("=== Verifying MoMo Signature ===");
            logger.info("Received signature: {}", receivedSignature);
            
            // QUAN TRỌNG: MoMo callback signature BẮT ĐẦU với accessKey
            String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&payType=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                accessKey,  // ← THÊM accessKey ở đầu
                params.get("amount"),
                params.get("extraData"),
                params.get("message"),
                params.get("orderId"),
                params.get("orderInfo"),
                params.get("orderType"),
                params.get("partnerCode"),
                params.get("payType"),
                params.get("requestId"),
                params.get("responseTime"),
                params.get("resultCode"),
                params.get("transId")
            );
            
            logger.info("Raw signature string: {}", rawSignature);
            
            String calculatedSignature = hmacSHA256(rawSignature, secretKey);
            logger.info("Calculated signature: {}", calculatedSignature);
            
            boolean isValid = calculatedSignature.equals(receivedSignature);
            logger.info("Signature verification result: {}", isValid);
            
            if (!isValid) {
                logger.error("SIGNATURE MISMATCH!");
                logger.error("Expected: {}", calculatedSignature);
                logger.error("Received: {}", receivedSignature);
            }
            
            return isValid;
            
        } catch (Exception e) {
            logger.error("Error verifying signature", e);
            return false;
        }
    }
    
    private String hmacSHA256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

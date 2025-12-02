package org.example.webtuthien.controller;

import org.example.webtuthien.model.Donation;
import org.example.webtuthien.service.CampaignService;
import org.example.webtuthien.service.DonationService;
import org.example.webtuthien.service.MoMoPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/payment/momo")
public class MoMoPaymentController {
    
    private static final Logger logger = LoggerFactory.getLogger(MoMoPaymentController.class);
    
    @Autowired
    private MoMoPaymentService momoService;
    
    @Autowired
    private DonationService donationService;
    
    @Autowired
    private CampaignService campaignService;
    
    // TEST ENDPOINT
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "MoMo Payment Controller is working! Time: " + System.currentTimeMillis();
    }
    
    @PostMapping("/create")
    @ResponseBody
    public Map<String, Object> createPayment(
            @RequestParam Long campaignId,
            @RequestParam Double amount,
            @RequestParam String donorName,
            @RequestParam(required = false) String message,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "Bạn cần đăng nhập để quyên góp");
                return response;
            }
            
            String orderId = "DONATE_" + System.currentTimeMillis();
            String orderInfo = "Ung ho chien dich #" + campaignId;
            String extraData = campaignId + "|" + donorName + "|" + (message != null ? message : "") + "|QR";
            
            logger.info("Creating MoMo QR payment: orderId={}, amount={}", orderId, amount.longValue());
            
            Map<String, Object> momoResponse = momoService.createPayment(
                orderId, amount.longValue(), orderInfo, extraData
            );
            
            Integer resultCode = (Integer) momoResponse.get("resultCode");
            if (resultCode != null && resultCode == 0) {
                response.put("success", true);
                response.put("payUrl", momoResponse.get("payUrl"));
                response.put("orderId", orderId);
            } else {
                response.put("success", false);
                response.put("message", "MoMo error: " + momoResponse.get("message"));
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error creating MoMo payment", e);
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return response;
        }
    }
    
    @PostMapping("/create-atm")
    @ResponseBody
    public Map<String, Object> createATMPayment(
            @RequestParam Long campaignId,
            @RequestParam Double amount,
            @RequestParam String donorName,
            @RequestParam(required = false) String message,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "Bạn cần đăng nhập để quyên góp");
                return response;
            }
            
            String orderId = "DONATE_ATM_" + System.currentTimeMillis();
            String orderInfo = "Ung ho chien dich #" + campaignId + " qua ATM";
            String extraData = campaignId + "|" + donorName + "|" + (message != null ? message : "") + "|ATM";
            
            logger.info("Creating MoMo ATM payment: orderId={}, amount={}", orderId, amount.longValue());
            
            Map<String, Object> momoResponse = momoService.createATMPayment(
                orderId, amount.longValue(), orderInfo, extraData
            );
            
            Integer resultCode = (Integer) momoResponse.get("resultCode");
            if (resultCode != null && resultCode == 0) {
                response.put("success", true);
                response.put("payUrl", momoResponse.get("payUrl"));
                response.put("orderId", orderId);
            } else {
                response.put("success", false);
                response.put("message", "MoMo ATM error: " + momoResponse.get("message"));
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error creating MoMo ATM payment", e);
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return response;
        }
    }
    
    // CALLBACK ENDPOINT - CRITICAL
    @GetMapping("/callback")
    public String handleCallback(HttpServletRequest request, Model model) {
        
        logger.info("========================================");
        logger.info("MOMO CALLBACK RECEIVED");
        logger.info("========================================");
        
        try {
            // Get all parameters
            Map<String, String> params = new HashMap<>();
            request.getParameterMap().forEach((key, values) -> {
                if (values.length > 0) {
                    params.put(key, values[0]);
                }
            });
            
            logger.info("All callback params: {}", params);
            
            String orderId = params.get("orderId");
            String resultCode = params.get("resultCode");
            String message = params.get("message");
            String extraData = params.get("extraData");
            String amountStr = params.get("amount");
            
            logger.info("Parsed - OrderID: {}, ResultCode: {}, Amount: {}", orderId, resultCode, amountStr);
            
            // Verify signature
            boolean isValid = momoService.verifySignature(params);
            logger.info("Signature verification: {}", isValid);
            
            if (isValid && "0".equals(resultCode)) {
                // SUCCESS
                logger.info("Payment SUCCESSFUL - Processing...");
                
                String[] parts = extraData.split("\\|");
                Long campaignId = Long.parseLong(parts[0]);
                String donorName = parts.length > 1 ? parts[1] : "Ẩn danh";
                String donationMessage = parts.length > 2 ? parts[2] : "";
                String paymentMethod = parts.length > 3 ? parts[3] : "";
                
                Long amount = Long.parseLong(amountStr);
                
                // Create donation
                Donation donation = new Donation();
                donation.setCampaignId(campaignId);
                donation.setDonorName(donorName);
                donation.setAmount(new BigDecimal(amount));
                donation.setMessage(donationMessage);
                
                Donation savedDonation = donationService.create(donation);
                logger.info("Donation created: ID={}", savedDonation.getId());
                
                // Update campaign
                campaignService.updateCurrentAmount(campaignId, new BigDecimal(amount));
                logger.info("Campaign updated: ID={}", campaignId);
                
                // Success response
                model.addAttribute("success", true);
                model.addAttribute("message", "Khoản đóng góp của bạn đã được xử lý thành công. Cảm ơn bạn đã đồng hành cùng chúng tôi!");
                model.addAttribute("campaignId", campaignId);
                model.addAttribute("amount", amount);
                model.addAttribute("orderId", orderId);
                model.addAttribute("paymentMethod", "MoMo " + ("ATM".equals(paymentMethod) ? "ATM" : "QR"));
                
                return "payment-result";
                
            } else {
                // FAILED
                logger.warn("Payment FAILED - ResultCode: {}, Message: {}", resultCode, message);
                
                Long campaignId = null;
                try {
                    String[] parts = extraData.split("\\|");
                    campaignId = Long.parseLong(parts[0]);
                } catch (Exception e) {
                    logger.error("Cannot parse campaignId", e);
                }
                
                model.addAttribute("success", false);
                model.addAttribute("message", "Thanh toán thất bại: " + message);
                model.addAttribute("campaignId", campaignId);
                model.addAttribute("orderId", orderId);
                
                return "payment-result";
            }
            
        } catch (Exception e) {
            logger.error("========================================");
            logger.error("ERROR processing callback", e);
            logger.error("========================================");
            
            model.addAttribute("success", false);
            model.addAttribute("message", "Lỗi xử lý kết quả: " + e.getMessage());
            return "payment-result";
        }
    }
    
    @PostMapping("/ipn")
    @ResponseBody
    public Map<String, Object> handleIPN(@RequestBody Map<String, String> params) {
        logger.info("MoMo IPN received: {}", params);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        return response;
    }
}

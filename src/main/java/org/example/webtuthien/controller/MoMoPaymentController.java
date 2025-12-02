package org.example.webtuthien.controller;

import org.example.webtuthien.service.MoMoPaymentService;
import org.example.webtuthien.service.CampaignService;
import org.example.webtuthien.service.DonationService;
import org.example.webtuthien.model.Donation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            // Kiểm tra đăng nhập
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "Bạn cần đăng nhập để quyên góp");
                return response;
            }
            
            // Tạo order ID unique
            String orderId = "DONATE_" + System.currentTimeMillis();
            String orderInfo = "Ung ho chien dich #" + campaignId;
            String extraData = campaignId + "|" + donorName + "|" + (message != null ? message : "");
            
            logger.info("Creating MoMo payment: orderId={}, amount={}, campaignId={}", orderId, amount.longValue(), campaignId);
            
            // Gọi MoMo API thật qua MoMoPaymentService
            Map<String, Object> momoResponse = momoService.createPayment(
                orderId, 
                amount.longValue(), 
                orderInfo, 
                extraData
            );
            
            logger.info("MoMo API response: {}", momoResponse);
            
            // Kiểm tra response từ MoMo
            Integer resultCode = (Integer) momoResponse.get("resultCode");
            if (resultCode != null && resultCode == 0) {
                response.put("success", true);
                response.put("payUrl", momoResponse.get("payUrl"));
                response.put("orderId", orderId);
                logger.info("MoMo payment URL created successfully: {}", momoResponse.get("payUrl"));
            } else {
                response.put("success", false);
                response.put("message", "MoMo error: " + momoResponse.get("message"));
                logger.error("MoMo API returned error: {}", momoResponse);
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error creating MoMo payment", e);
            response.put("success", false);
            response.put("message", "Lỗi tạo thanh toán: " + e.getMessage());
            return response;
        }
    }
    
    @GetMapping("/callback")
    public String handleCallback(@RequestParam Map<String, String> params, 
                                 org.springframework.ui.Model model) {
        try {
            logger.info("=== MoMo Callback Received ===");
            logger.info("All params: {}", params);
            
            String orderId = params.get("orderId");
            String resultCode = params.get("resultCode");
            String message = params.get("message");
            String extraData = params.get("extraData");
            String amountStr = params.get("amount");
            String payType = params.get("payType");
            
            logger.info("Order ID: {}", orderId);
            logger.info("Result Code: {}", resultCode);
            
            // Verify signature
            boolean isValid = momoService.verifySignature(params);
            logger.info("Signature valid: {}", isValid);
            
            if (isValid && "0".equals(resultCode)) {
                // Payment successful
                try {
                    String[] parts = extraData.split("\\|");
                    Long campaignId = Long.parseLong(parts[0]);
                    String donorName = parts.length > 1 ? parts[1] : "Ẩn danh";
                    String donationMessage = parts.length > 2 ? parts[2] : "";
                    String paymentMethod = parts.length > 3 ? parts[3] : "";
                    
                    Long amount = Long.parseLong(amountStr);
                    
                    logger.info("Creating donation - Campaign: {}, Donor: {}, Amount: {}", 
                               campaignId, donorName, amount);
                    
                    // Create donation record
                    Donation donation = new Donation();
                    donation.setCampaignId(campaignId);
                    donation.setDonorName(donorName);
                    donation.setAmount(new BigDecimal(amount));
                    donation.setMessage(donationMessage);
                    
                    Donation savedDonation = donationService.create(donation);
                    logger.info("Donation created with ID: {}", savedDonation.getId());
                    
                    // Update campaign amount
                    campaignService.updateCurrentAmount(campaignId, new BigDecimal(amount));
                    logger.info("Campaign {} updated successfully", campaignId);
                    
                    // Redirect to thank you page
                    model.addAttribute("success", true);
                    model.addAttribute("message", "Khoản đóng góp của bạn đã được xử lý thành công. Cảm ơn bạn đã đồng hành cùng chúng tôi!");
                    model.addAttribute("campaignId", campaignId);
                    model.addAttribute("amount", amount);
                    model.addAttribute("orderId", orderId);
                    model.addAttribute("paymentMethod", "MoMo " + ("ATM".equals(paymentMethod) ? "ATM" : "QR"));
                    
                    return "payment-result";
                    
                } catch (Exception e) {
                    logger.error("Error processing successful payment", e);
                    model.addAttribute("success", false);
                    model.addAttribute("message", "Thanh toán thành công nhưng có lỗi khi lưu dữ liệu. Vui lòng liên hệ admin với mã giao dịch: " + orderId);
                    return "payment-result";
                }
            } else {
                // Payment failed
                logger.warn("Payment failed - Result code: {}, Message: {}", resultCode, message);
                
                Long campaignId = null;
                try {
                    String[] parts = extraData.split("\\|");
                    campaignId = Long.parseLong(parts[0]);
                } catch (Exception e) {
                    logger.error("Cannot parse campaignId from extraData", e);
                }
                
                model.addAttribute("success", false);
                model.addAttribute("message", "Thanh toán thất bại: " + message);
                model.addAttribute("campaignId", campaignId);
                model.addAttribute("orderId", orderId);
                
                return "payment-result";
            }
        } catch (Exception e) {
            logger.error("Error processing MoMo callback", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "Lỗi xử lý kết quả thanh toán: " + e.getMessage());
            return "payment-result";
        }
    }
    
    @PostMapping("/ipn")
    @ResponseBody
    public Map<String, Object> handleIPN(@RequestBody Map<String, String> params) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("MoMo IPN received: {}", params);
            
            boolean isValid = momoService.verifySignature(params);
            String resultCode = params.get("resultCode");
            
            if (isValid && "0".equals(resultCode)) {
                response.put("status", "success");
                logger.info("MoMo IPN verified successfully for order: {}", params.get("orderId"));
            } else {
                response.put("status", "failed");
                logger.warn("MoMo IPN verification failed");
            }
            
            return response;
        } catch (Exception e) {
            logger.error("Error processing MoMo IPN", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return response;
        }
    }
    
    @PostMapping("/create-phone")
    @ResponseBody
    public Map<String, Object> createPhonePayment(
            @RequestParam Long campaignId,
            @RequestParam Double amount,
            @RequestParam String donorName,
            @RequestParam(required = false) String message,
            @RequestParam String phone,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== MoMo Phone Payment Request ===");
            System.out.println("Campaign ID: " + campaignId);
            System.out.println("Amount: " + amount);
            System.out.println("Phone: " + phone);
            
            // TODO: Implement MoMo phone payment
            
            response.put("success", true);
            response.put("message", "Yêu cầu đã được gửi đến số " + phone);
            
        } catch (Exception e) {
            System.err.println("Error creating phone payment: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        
        return response;
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
            // Kiểm tra đăng nhập
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "Bạn cần đăng nhập để quyên góp");
                return response;
            }
            
            // Tạo order ID unique
            String orderId = "DONATE_ATM_" + System.currentTimeMillis();
            String orderInfo = "Ung ho chien dich #" + campaignId + " qua ATM";
            String extraData = campaignId + "|" + donorName + "|" + (message != null ? message : "") + "|ATM";
            
            logger.info("Creating MoMo ATM payment: orderId={}, amount={}, campaignId={}", orderId, amount.longValue(), campaignId);
            
            // Gọi MoMo API với requestType = "payWithATM"
            Map<String, Object> momoResponse = momoService.createATMPayment(
                orderId, 
                amount.longValue(), 
                orderInfo, 
                extraData
            );
            
            logger.info("MoMo ATM API response: {}", momoResponse);
            
            Integer resultCode = (Integer) momoResponse.get("resultCode");
            if (resultCode != null && resultCode == 0) {
                response.put("success", true);
                response.put("payUrl", momoResponse.get("payUrl"));
                response.put("orderId", orderId);
                logger.info("MoMo ATM payment URL created successfully");
            } else {
                response.put("success", false);
                response.put("message", "MoMo ATM error: " + momoResponse.get("message"));
                logger.error("MoMo ATM API returned error: {}", momoResponse);
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error creating MoMo ATM payment", e);
            response.put("success", false);
            response.put("message", "Lỗi tạo thanh toán MoMo ATM: " + e.getMessage());
            return response;
        }
    }
}

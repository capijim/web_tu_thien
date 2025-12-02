package org.example.webtuthien.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.webtuthien.model.Donation;
import org.example.webtuthien.service.DonationService;
import org.example.webtuthien.service.VNPayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/vnpay")
public class VNPayController {
    
    private static final Logger logger = LoggerFactory.getLogger(VNPayController.class);
    
    private final VNPayService vnPayService;
    private final DonationService donationService;
    
    public VNPayController(VNPayService vnPayService, DonationService donationService) {
        this.vnPayService = vnPayService;
        this.donationService = donationService;
    }
    
    @PostMapping("/create-payment")
    @ResponseBody
    public Map<String, Object> createPayment(
            @RequestBody Map<String, Object> paymentRequest,
            HttpSession session,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check authentication
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "Bạn cần đăng nhập để quyên góp");
                return response;
            }
            
            Long campaignId = ((Number) paymentRequest.get("campaignId")).longValue();
            Double amount = ((Number) paymentRequest.get("amount")).doubleValue();
            String donorName = (String) paymentRequest.get("donorName");
            String message = (String) paymentRequest.get("message");
            
            logger.info("Creating VNPay payment - Campaign: {}, Amount: {}, Donor: {}", 
                       campaignId, amount, donorName);
            
            // Create donation record with PENDING status
            Donation donation = new Donation();
            donation.setCampaignId(campaignId);
            donation.setDonorName(donorName);
            donation.setAmount(BigDecimal.valueOf(amount));
            donation.setMessage(message);
            
            Donation savedDonation = donationService.create(donation);
            logger.info("Donation created with ID: {}", savedDonation.getId());
            
            // Create VNPay payment URL
            String paymentUrl = vnPayService.createPaymentUrl(savedDonation, request);
            
            response.put("success", true);
            response.put("paymentUrl", paymentUrl);
            response.put("donationId", savedDonation.getId());
            
        } catch (Exception e) {
            logger.error("Error creating VNPay payment", e);
            response.put("success", false);
            response.put("message", "Lỗi tạo thanh toán VNPay: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        try {
            logger.info("=== VNPay Return ===");
            
            Map<String, Object> result = vnPayService.handlePaymentReturn(request);
            boolean success = (boolean) result.get("success");
            String message = (String) result.get("message");
            Long campaignId = (Long) result.getOrDefault("campaignId", null);
            
            logger.info("VNPay return result: success={}, campaignId={}", success, campaignId);
            
            // Extract additional info
            String vnpAmount = request.getParameter("vnp_Amount");
            String vnpTxnRef = request.getParameter("vnp_TxnRef");
            
            Long amount = null;
            if (vnpAmount != null) {
                try {
                    amount = Long.parseLong(vnpAmount) / 100; // VNPay trả về amount đã nhân 100
                } catch (NumberFormatException e) {
                    logger.error("Cannot parse amount", e);
                }
            }
            
            model.addAttribute("success", success);
            model.addAttribute("message", message);
            model.addAttribute("campaignId", campaignId);
            model.addAttribute("amount", amount);
            model.addAttribute("orderId", vnpTxnRef);
            model.addAttribute("paymentMethod", "VNPay");
            
            return "payment-result";
            
        } catch (Exception e) {
            logger.error("Error in VNPay return", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "Lỗi xử lý thanh toán: " + e.getMessage());
            return "payment-result";
        }
    }
}

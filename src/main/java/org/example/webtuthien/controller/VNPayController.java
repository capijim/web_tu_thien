package org.example.webtuthien.controller;

import org.example.webtuthien.model.Donation;
import org.example.webtuthien.service.DonationService;
import org.example.webtuthien.service.VNPayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/vnpay")
public class VNPayController {
    
    private final VNPayService vnPayService;
    private final DonationService donationService;
    
    public VNPayController(VNPayService vnPayService, DonationService donationService) {
        this.vnPayService = vnPayService;
        this.donationService = donationService;
    }
    
    @PostMapping("/create-payment")
    @ResponseBody
    public Map<String, Object> createPayment(@RequestBody Donation donation, 
                                              HttpServletRequest request,
                                              HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== VNPay Payment Request (TEST MODE) ===");
            System.out.println("Campaign ID: " + donation.getCampaignId());
            System.out.println("Donor Name: " + donation.getDonorName());
            System.out.println("Amount: " + donation.getAmount());
            System.out.println("Message: " + donation.getMessage());
            
            // Kiểm tra đăng nhập
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "Bạn cần đăng nhập để quyên góp");
                return response;
            }
            
            // Validate input
            if (donation.getCampaignId() == null) {
                response.put("success", false);
                response.put("message", "Campaign ID is required");
                return response;
            }
            
            if (donation.getAmount() == null || donation.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                response.put("success", false);
                response.put("message", "Amount must be greater than 0");
                return response;
            }
            
            // Tạo donation trước và lưu vào DB
            System.out.println("Creating donation...");
            Donation createdDonation = donationService.create(donation);
            System.out.println("Donation created with ID: " + createdDonation.getId());
            
            // Kiểm tra donation đã có ID
            if (createdDonation.getId() == null) {
                response.put("success", false);
                response.put("message", "Lỗi: Không thể tạo donation");
                return response;
            }
            
            // Tạo URL thanh toán VNPay (không lưu payment vào DB)
            System.out.println("Creating VNPay payment URL (TEST MODE - no DB save)...");
            String paymentUrl = vnPayService.createPaymentUrl(createdDonation, request);
            System.out.println("Payment URL created successfully");
            System.out.println("URL: " + paymentUrl);
            
            response.put("success", true);
            response.put("paymentUrl", paymentUrl);
            response.put("donationId", createdDonation.getId());
            response.put("testMode", true);
            
        } catch (Exception e) {
            System.err.println("=== Error in create-payment ===");
            System.err.println("Error class: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Lỗi tạo thanh toán: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/return")
    public String paymentReturn(HttpServletRequest request, Model model) {
        Map<String, Object> result = vnPayService.handlePaymentReturn(request);
        
        model.addAttribute("success", result.get("success"));
        model.addAttribute("message", result.get("message"));
        
        if (result.containsKey("campaignId")) {
            model.addAttribute("campaignId", result.get("campaignId"));
        }
        
        return "payment-result";
    }
}

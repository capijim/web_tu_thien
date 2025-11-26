package org.example.webtuthien.controller;

import org.example.webtuthien.model.Donation;
import org.example.webtuthien.service.DonationService;
import org.example.webtuthien.service.VNPayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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
            // Kiểm tra đăng nhập
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                response.put("success", false);
                response.put("message", "Bạn cần đăng nhập để quyên góp");
                return response;
            }
            
            // Tạo donation trước
            Donation createdDonation = donationService.create(donation);
            
            // Tạo URL thanh toán VNPay
            String paymentUrl = vnPayService.createPaymentUrl(createdDonation, request);
            
            response.put("success", true);
            response.put("paymentUrl", paymentUrl);
            response.put("donationId", createdDonation.getId());
            
        } catch (Exception e) {
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
        
        if (result.containsKey("donationId")) {
            model.addAttribute("donationId", result.get("donationId"));
        }
        
        return "payment-result";
    }
}

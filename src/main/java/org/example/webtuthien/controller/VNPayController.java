package org.example.webtuthien.controller;

import org.example.webtuthien.service.*;
import org.example.webtuthien.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/vnpay")
public class VNPayController {
    
    private static final Logger logger = LoggerFactory.getLogger(VNPayController.class);
    
    @Autowired
    private VNPayService vnPayService;
    
    @Autowired
    private DonationService donationService;
    
    @Autowired
    private CampaignService campaignService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private EmailService emailService;
    
    @PostMapping("/create-payment")
    @ResponseBody
    public Map<String, Object> createPayment(@RequestBody Map<String, Object> request, HttpSession session) {
        try {
            Long campaignId = Long.valueOf(request.get("campaignId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String message = (String) request.get("message");
            
            // Get user info
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj == null) {
                return Map.of("success", false, "message", "Vui lòng đăng nhập");
            }
            
            Long userId = Long.valueOf(userIdObj.toString());
            
            // Store donation info in session for callback
            session.setAttribute("pendingDonation", Map.of(
                "campaignId", campaignId,
                "userId", userId,
                "amount", amount,
                "message", message != null ? message : ""
            ));
            
            // Create payment URL
            String paymentUrl = vnPayService.createPaymentUrl(campaignId, amount, message);
            
            return Map.of("success", true, "paymentUrl", paymentUrl);
        } catch (Exception e) {
            logger.error("Error creating payment", e);
            return Map.of("success", false, "message", "Lỗi tạo thanh toán: " + e.getMessage());
        }
    }
    
    @GetMapping("/return")
    public String paymentReturn(HttpServletRequest request, HttpSession session, Model model) {
        try {
            // Verify payment
            Map<String, String> params = vnPayService.getParamsFromRequest(request);
            boolean isValid = vnPayService.verifyPayment(params);
            String responseCode = params.get("vnp_ResponseCode");
            
            if (isValid && "00".equals(responseCode)) {
                // Payment successful - process donation
                @SuppressWarnings("unchecked")
                Map<String, Object> pendingDonation = (Map<String, Object>) session.getAttribute("pendingDonation");
                
                if (pendingDonation != null) {
                    Long campaignId = ((Number) pendingDonation.get("campaignId")).longValue();
                    Long userId = ((Number) pendingDonation.get("userId")).longValue();
                    BigDecimal amount = (BigDecimal) pendingDonation.get("amount");
                    String message = (String) pendingDonation.get("message");
                    
                    // Get user and campaign info
                    var userOpt = userService.getUserById(userId);
                    var campaignOpt = campaignService.findById(campaignId);
                    
                    if (userOpt.isPresent() && campaignOpt.isPresent()) {
                        User user = userOpt.get();
                        Campaign campaign = campaignOpt.get();
                        
                        // Create donation
                        Donation donation = new Donation();
                        donation.setCampaignId(campaignId);
                        donation.setDonorName(user.getName());
                        donation.setAmount(amount);
                        donation.setMessage(message);
                        
                        Donation createdDonation = donationService.create(donation);
                        
                        // Update campaign amount
                        campaignService.updateCurrentAmount(campaignId, amount);
                        
                        // Send email notification
                        try {
                            emailService.sendDonationSuccessEmail(
                                user.getEmail(),
                                user.getName(),
                                amount,
                                campaign.getTitle(),
                                campaign.getCategory(),
                                campaignId,
                                createdDonation.getId(),
                                message,
                                createdDonation.getCreatedAt()
                            );
                            logger.info("Email sent successfully to: {}", user.getEmail());
                        } catch (Exception e) {
                            logger.error("Failed to send email", e);
                        }
                        
                        // Clear pending donation
                        session.removeAttribute("pendingDonation");
                        
                        model.addAttribute("success", true);
                        model.addAttribute("message", "Thanh toán thành công! Cảm ơn bạn đã quyên góp " + 
                            String.format("%,d", amount.longValue()) + " VNĐ. Email xác nhận đã được gửi đến " + user.getEmail());
                    } else {
                        model.addAttribute("success", false);
                        model.addAttribute("message", "Không tìm thấy thông tin người dùng hoặc chiến dịch");
                    }
                } else {
                    model.addAttribute("success", false);
                    model.addAttribute("message", "Không tìm thấy thông tin thanh toán");
                }
            } else {
                model.addAttribute("success", false);
                model.addAttribute("message", "Thanh toán thất bại. Mã lỗi: " + responseCode);
            }
            
        } catch (Exception e) {
            logger.error("Error processing payment return", e);
            model.addAttribute("success", false);
            model.addAttribute("message", "Lỗi xử lý kết quả thanh toán: " + e.getMessage());
        }
        
        return "payment-result";
    }
}

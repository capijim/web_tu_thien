package org.example.webtuthien.controller;

import jakarta.servlet.http.HttpSession;
import org.example.webtuthien.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminReportController.class);
    
    @Autowired
    private ReportService reportService;
    
    @GetMapping("/donations/excel")
    public ResponseEntity<byte[]> downloadDonationsExcel(HttpSession session) {
        try {
            // Check admin authentication
            Object adminId = session.getAttribute("adminId");
            if (adminId == null) {
                return ResponseEntity.status(401).build();
            }
            
            logger.info("Admin {} requesting donations Excel report", adminId);
            
            byte[] reportBytes = reportService.generateDonationsExcelReport();
            
            String filename = "Donations_Report_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(reportBytes.length);
            
            logger.info("Sending Excel report: {}", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(reportBytes);
                
        } catch (Exception e) {
            logger.error("Error generating donations Excel report", e);
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/donations/excel/campaign/{campaignId}")
    public ResponseEntity<byte[]> downloadDonationsByCampaignExcel(
            @PathVariable Long campaignId, 
            HttpSession session) {
        try {
            // Check admin authentication
            Object adminId = session.getAttribute("adminId");
            if (adminId == null) {
                return ResponseEntity.status(401).build();
            }
            
            logger.info("Admin {} requesting donations Excel report for campaign {}", adminId, campaignId);
            
            byte[] reportBytes = reportService.generateDonationsByCampaignExcelReport(campaignId);
            
            String filename = "Donations_Campaign_" + campaignId + "_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(reportBytes.length);
            
            logger.info("Sending Excel report: {}", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(reportBytes);
                
        } catch (Exception e) {
            logger.error("Error generating donations Excel report for campaign {}", campaignId, e);
            return ResponseEntity.status(500).build();
        }
    }
}

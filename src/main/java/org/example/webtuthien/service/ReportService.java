package org.example.webtuthien.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.example.webtuthien.model.Donation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    
    @Autowired
    private DonationService donationService;
    
    public byte[] generateDonationsExcelReport() throws Exception {
        logger.info("Generating donations Excel report...");
        
        // Load donations data
        List<Donation> donations = donationService.list();
        logger.info("Found {} donations", donations.size());
        
        // Load JasperReport template
        InputStream reportStream = new ClassPathResource("reports/donations.jrxml").getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
        
        // Create data source
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(donations);
        
        // Parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Danh sách quyên góp");
        
        // Fill report
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        
        // Export to Excel
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRXlsxExporter exporter = new JRXlsxExporter();
        
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        
        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setOnePagePerSheet(false);
        configuration.setDetectCellType(true);
        configuration.setCollapseRowSpan(false);
        configuration.setWhitePageBackground(false);
        exporter.setConfiguration(configuration);
        
        exporter.exportReport();
        
        logger.info("Excel report generated successfully");
        return outputStream.toByteArray();
    }
    
    public byte[] generateDonationsByCampaignExcelReport(Long campaignId) throws Exception {
        logger.info("Generating donations Excel report for campaign {}", campaignId);
        
        // Load donations data for specific campaign
        List<Donation> donations = donationService.findByCampaignId(campaignId);
        logger.info("Found {} donations for campaign {}", donations.size(), campaignId);
        
        // Load JasperReport template
        InputStream reportStream = new ClassPathResource("reports/donations.jrxml").getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
        
        // Create data source
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(donations);
        
        // Parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Danh sách quyên góp - Campaign " + campaignId);
        
        // Fill report
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        
        // Export to Excel
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRXlsxExporter exporter = new JRXlsxExporter();
        
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        
        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setOnePagePerSheet(false);
        configuration.setDetectCellType(true);
        configuration.setCollapseRowSpan(false);
        configuration.setWhitePageBackground(false);
        exporter.setConfiguration(configuration);
        
        exporter.exportReport();
        
        logger.info("Excel report generated successfully for campaign {}", campaignId);
        return outputStream.toByteArray();
    }
}

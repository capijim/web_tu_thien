package org.example.webtuthien.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EtherealEmailConfig {
    
    @Value("${spring.mail.host}")
    private String mailHost;
    
    @Value("${spring.mail.port}")
    private int mailPort;
    
    @Value("${spring.mail.username}")
    private String mailUsername;
    
    @Value("${spring.mail.password}")
    private String mailPassword;
    
    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);
        mailSender.setUsername(mailUsername);
        mailSender.setPassword(mailPassword);
        mailSender.setDefaultEncoding("UTF-8");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", mailHost);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.debug", "true");
        
        System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║              📧 GMAIL SMTP CONFIGURED                         ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════╣");
        System.out.println("║ Host:     " + mailHost);
        System.out.println("║ Port:     " + mailPort);
        System.out.println("║ Username: " + mailUsername);
        System.out.println("║ Password: " + (mailPassword != null && !mailPassword.isEmpty() ? "***" + mailPassword.substring(Math.max(0, mailPassword.length() - 4)) : "NOT SET"));
        System.out.println("╠═══════════════════════════════════════════════════════════════╣");
        System.out.println("║ ✅ Real emails will be sent via Gmail!                       ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝\n");
        
        return mailSender;
    }
}

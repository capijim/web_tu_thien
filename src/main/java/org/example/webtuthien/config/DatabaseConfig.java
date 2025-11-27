package org.example.webtuthien.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    
    @Value("${spring.datasource.username}")
    private String username;
    
    @Value("${spring.datasource.password}")
    private String password;
    
    @Bean
    @Profile("local")
    public DataSource localDataSource() {
        logger.info("=============================================================");
        logger.info("Initializing Supabase Database Connection");
        logger.info("JDBC URL: {}", maskUrl(jdbcUrl));
        logger.info("Username: {}", username);
        logger.info("=============================================================");
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        
        // Connection pool settings optimized for Supabase
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);
        
        // Connection test query
        config.setConnectionTestQuery("SELECT 1");
        
        // SSL for Supabase
        config.addDataSourceProperty("ssl", "true");
        config.addDataSourceProperty("sslmode", "require");
        
        try {
            HikariDataSource dataSource = new HikariDataSource(config);
            logger.info("✓ Successfully connected to Supabase database!");
            return dataSource;
        } catch (Exception e) {
            logger.error("=============================================================");
            logger.error("❌ Failed to create DataSource!");
            logger.error("URL: {}", jdbcUrl);
            logger.error("Username: {}", username);
            logger.error("Error: {}", e.getMessage());
            logger.error("=============================================================");
            logger.error("💡 Troubleshooting steps:");
            logger.error("   1. Verify Supabase project is active");
            logger.error("   2. Check database password is correct");
            logger.error("   3. Ensure database has pooler enabled");
            logger.error("   4. Verify network connectivity to Supabase");
            throw e;
        }
    }
    
    private String maskUrl(String url) {
        if (url == null) return "null";
        // Mask password if present in URL
        return url.replaceAll(":[^:@]+@", ":****@");
    }
}

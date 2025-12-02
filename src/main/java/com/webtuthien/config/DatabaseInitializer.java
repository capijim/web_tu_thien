package com.webtuthien.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            logger.info("🔍 Checking database schema...");
            
            // Check if tables exist
            Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public'",
                Integer.class
            );

            if (tableCount == null || tableCount == 0) {
                logger.warn("⚠️ No tables found. Running database initialization...");
                initializeDatabase();
            } else {
                logger.info("✅ Database already initialized. Found {} tables.", tableCount);
                
                // Verify critical tables exist
                verifyCriticalTables();
            }
            
        } catch (Exception e) {
            logger.error("❌ Error during database initialization: {}", e.getMessage(), e);
            // Don't throw exception - let app continue, health check will report the issue
        }
    }

    private void initializeDatabase() {
        try {
            logger.info("📦 Running V1__init_schema.sql...");
            executeSqlFile("db/migration/V1__init_schema.sql");
            logger.info("✅ Schema initialized successfully.");

            logger.info("📦 Running V2__seed_data.sql...");
            executeSqlFile("db/migration/V2__seed_data.sql");
            logger.info("✅ Seed data inserted successfully.");

            logger.info("📦 Running V3__fix_payments_table.sql...");
            try {
                executeSqlFile("db/migration/V3__fix_payments_table.sql");
                logger.info("✅ Payments table fixed successfully.");
            } catch (Exception e) {
                logger.warn("⚠️ V3 migration skipped or already applied: {}", e.getMessage());
            }

            logger.info("🎉 Database initialization completed!");
            
        } catch (Exception e) {
            logger.error("❌ Failed to initialize database: {}", e.getMessage(), e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void executeSqlFile(String filePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(filePath);
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            String sql = reader.lines().collect(Collectors.joining("\n"));
            
            // Execute SQL (PostgreSQL supports multi-statement execution)
            jdbcTemplate.execute(sql);
            
        } catch (Exception e) {
            logger.error("Failed to execute SQL file: {}", filePath, e);
            throw e;
        }
    }

    private void verifyCriticalTables() {
        String[] criticalTables = {"users", "partners", "campaigns", "donations", "admins", "payments"};
        
        for (String table : criticalTables) {
            try {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?",
                    Integer.class,
                    table
                );
                
                if (count == null || count == 0) {
                    logger.error("❌ Critical table missing: {}", table);
                } else {
                    logger.debug("✅ Table exists: {}", table);
                }
            } catch (Exception e) {
                logger.error("❌ Error checking table {}: {}", table, e.getMessage());
            }
        }
    }
}

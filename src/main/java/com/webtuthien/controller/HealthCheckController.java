package com.webtuthien.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/db-info")
    public Map<String, Object> checkDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            result.put("status", "SUCCESS");
            result.put("connected", true);
            
            DatabaseMetaData metaData = connection.getMetaData();
            result.put("databaseProductName", metaData.getDatabaseProductName());
            result.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            result.put("driverName", metaData.getDriverName());
            result.put("driverVersion", metaData.getDriverVersion());
            result.put("url", metaData.getURL());
            result.put("username", metaData.getUserName());
            
            // Test query
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public'", 
                Integer.class
            );
            result.put("tablesCount", count);
            
            // Check specific tables
            Map<String, Boolean> tables = new HashMap<>();
            tables.put("users", checkTableExists("users"));
            tables.put("campaigns", checkTableExists("campaigns"));
            tables.put("donations", checkTableExists("donations"));
            tables.put("admins", checkTableExists("admins"));
            result.put("tables", tables);
            
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("connected", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            
            if (e.getCause() != null) {
                result.put("rootCause", e.getCause().getMessage());
            }
        }
        
        return result;
    }

    @GetMapping("/db-test")
    public Map<String, Object> simpleTest() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String version = jdbcTemplate.queryForObject("SELECT version()", String.class);
            result.put("status", "SUCCESS");
            result.put("postgresVersion", version);
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    private boolean checkTableExists(String tableName) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = ?",
                Integer.class,
                tableName
            );
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
}

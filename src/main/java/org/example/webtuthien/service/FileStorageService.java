package org.example.webtuthien.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir:/app/uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
            logger.info("==============================================");
            logger.info("Upload directory initialized");
            logger.info("Path: {}", this.fileStorageLocation);
            logger.info("Exists: {}", Files.exists(this.fileStorageLocation));
            logger.info("Writable: {}", Files.isWritable(this.fileStorageLocation));
            logger.info("Readable: {}", Files.isReadable(this.fileStorageLocation));
            logger.info("==============================================");
        } catch (Exception ex) {
            logger.error("ERROR: Could not create upload directory at: {}", uploadDir, ex);
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Get original filename
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        
        try {
            // Check if filename contains invalid characters
            if (originalFileName.contains("..")) {
                throw new IllegalArgumentException("Filename contains invalid path sequence: " + originalFileName);
            }

            // Generate unique filename
            String fileExtension = "";
            int lastDotIndex = originalFileName.lastIndexOf('.');
            if (lastDotIndex > 0) {
                fileExtension = originalFileName.substring(lastDotIndex);
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Copy file to target location
            Path targetLocation = this.fileStorageLocation.resolve(newFileName);
            
            logger.info("Storing file: {}", originalFileName);
            logger.info("New filename: {}", newFileName);
            logger.info("Target location: {}", targetLocation);
            
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            logger.info("File saved successfully!");
            logger.info("File size: {} bytes", Files.size(targetLocation));
            
            // Return relative URL path
            return "/uploads/" + newFileName;
        } catch (IOException ex) {
            logger.error("ERROR saving file: {}", originalFileName, ex);
            throw new RuntimeException("Could not store file " + originalFileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            logger.info("Deleted file: {}", filePath);
        } catch (IOException ex) {
            logger.error("Could not delete file: {}", fileName, ex);
        }
    }

    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }
}

package com.thecodereveal.shopease.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.net.URL;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${file.upload.mode:local}")
    private String uploadMode;

    @Value("${file.upload.local-dir:uploads}")
    private String localUploadDirectory;

    @Value("${file.upload.cdn-url:http://localhost:8080/uploads}")
    private String cdnUrl;

    @Value("${file.upload.zone:dummy}")
    private String storageZone;

    @Value("${file.upload.api-key:dummy}")
    private String fileUploadKey;

    @Value("${file.upload.host-url:https://dummy.com}")
    private String fileHostName;

    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("The uploaded file is empty");
        }

        String safeFileName = UUID.randomUUID() + "-" + sanitizeFileName(fileName);
        if ("local".equalsIgnoreCase(uploadMode)) {
            Path uploadDirectory = Path.of(localUploadDirectory).toAbsolutePath().normalize();
            Files.createDirectories(uploadDirectory);
            Path destination = uploadDirectory.resolve(safeFileName).normalize();
            if (!destination.startsWith(uploadDirectory)) {
                throw new IllegalArgumentException("Invalid file name");
            }

            try (var inputStream = file.getInputStream()) {
                Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            return cdnUrl.replaceAll("/$", "") + "/" + safeFileName;
        }

        if (!"bunny".equalsIgnoreCase(uploadMode)) {
            throw new IllegalStateException("Unsupported file upload mode: " + uploadMode);
        }

        String urlString = fileHostName + "/" + storageZone + "/" + safeFileName;
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("AccessKey", fileUploadKey);
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setDoOutput(true);
        try {
            try (BufferedInputStream inputStream = new BufferedInputStream(file.getInputStream());
                 BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream())) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_CREATED) {
                throw new IOException("File storage returned HTTP " + responseCode);
            }
            return cdnUrl.replaceAll("/$", "") + "/" + safeFileName;
        } finally {
            connection.disconnect();
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "upload.bin";
        }
        String baseName = Path.of(fileName).getFileName().toString();
        String sanitized = baseName.replaceAll("[^A-Za-z0-9._-]", "-");
        return sanitized.isBlank() ? "upload.bin" : sanitized;
    }
}

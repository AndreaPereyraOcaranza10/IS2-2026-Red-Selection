package com.thecodereveal.shopease.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class LocalUploadResourceConfig implements WebMvcConfigurer {

    @Value("${file.upload.mode:local}")
    private String uploadMode;

    @Value("${file.upload.local-dir:uploads}")
    private String localUploadDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if ("local".equalsIgnoreCase(uploadMode)) {
            String location = Path.of(localUploadDirectory).toAbsolutePath().normalize().toUri().toString();
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations(location.endsWith("/") ? location : location + "/");
        }
    }
}

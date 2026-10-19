package com.thecodereveal.shopease.controllers;

import com.thecodereveal.shopease.services.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
@CrossOrigin
public class FileUpload {

    @Autowired
    FileUploadService fileUploadService;

    @PostMapping
    public ResponseEntity<Map<String, String>> fileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("fileName") String fileName) throws IOException {
        String url = fileUploadService.uploadFile(file, fileName);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", url));
    }
}

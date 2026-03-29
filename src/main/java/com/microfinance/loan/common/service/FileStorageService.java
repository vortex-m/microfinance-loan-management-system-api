package com.microfinance.loan.common.service;

import com.microfinance.loan.config.FileUploadConfig;
import com.microfinance.loan.common.enums.FileType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    @Autowired
    private FileUploadConfig fileUploadConfig;

    private String uploadDir;

    @PostConstruct
    public void init() {
        this.uploadDir = fileUploadConfig.getUploadDir();

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String storeFile(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFileName.contains("..")) {
            throw new IllegalArgumentException("Invalid file path");
        }
        String extension = getExtension(originalFileName);
        String contentType = file.getContentType();
        if (!FileType.isValid(contentType, extension)) {
            throw new IllegalArgumentException("Only PDF and image files are allowed");
        }
        String fileName = UUID.randomUUID() + "_" + originalFileName;
        String filePath = uploadDir + File.separator + fileName;

        file.transferTo(new File(filePath));

        return fileName;
    }


    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            throw new IllegalArgumentException("File has no extension");
        }
        return fileName.substring(index + 1).toLowerCase();
    }
}
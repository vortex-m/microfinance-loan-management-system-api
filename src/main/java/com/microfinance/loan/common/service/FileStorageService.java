package com.microfinance.loan.common.service;

import com.microfinance.loan.config.FileUploadConfig;
import com.microfinance.loan.common.enums.FileType;
import jakarta.annotation.PostConstruct;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

    private final FileUploadConfig fileUploadConfig;

    private Path uploadDir;
    private S3Client s3Client;

    public FileStorageService(FileUploadConfig fileUploadConfig) {
        this.fileUploadConfig = fileUploadConfig;
    }

    @PostConstruct
    public void init() {
        String configuredUploadDir = fileUploadConfig.getUploadDir();
        String safeUploadDir = StringUtils.hasText(configuredUploadDir) ? configuredUploadDir : "upload";
        this.uploadDir = Paths.get(safeUploadDir).toAbsolutePath().normalize();

        if ("s3".equalsIgnoreCase(fileUploadConfig.getStorageType())) {
            initS3Client();
            return;
        }

        try {
            Files.createDirectories(uploadDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not initialize upload directory: " + uploadDir, ex);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        return storeFile(file, null);
    }

    public String storeFile(MultipartFile file, String folderPrefix) throws IOException {

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

        if ("s3".equalsIgnoreCase(fileUploadConfig.getStorageType())) {
            return uploadToS3(file, fileName, contentType, folderPrefix);
        }

        Path targetPath = uploadDir.resolve(fileName).normalize();
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toUri().toString();
    }

    private void initS3Client() {
        if (!StringUtils.hasText(fileUploadConfig.getBucketName()) || !StringUtils.hasText(fileUploadConfig.getRegion())) {
            throw new IllegalStateException("S3 storage selected but bucket/region is missing in configuration");
        }

        Region awsRegion = Region.of(fileUploadConfig.getRegion());

        if (StringUtils.hasText(fileUploadConfig.getAccessKey()) && StringUtils.hasText(fileUploadConfig.getSecretKey())) {
            this.s3Client = S3Client.builder()
                    .region(awsRegion)
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(fileUploadConfig.getAccessKey(), fileUploadConfig.getSecretKey())
                    ))
                    .build();
            return;
        }

        this.s3Client = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    private String uploadToS3(MultipartFile file, String fileName, String contentType, String folderPrefix) throws IOException {
        String effectivePrefix = StringUtils.hasText(folderPrefix) ? folderPrefix : "uploads";
        String configuredPrefix = StringUtils.hasText(fileUploadConfig.getS3Prefix()) ? fileUploadConfig.getS3Prefix().trim() : "";

        String key = buildObjectKey(configuredPrefix, effectivePrefix, fileName);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(fileUploadConfig.getBucketName())
                .key(key)
                .contentType(StringUtils.hasText(contentType) ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        return "https://" + fileUploadConfig.getBucketName() + ".s3." + fileUploadConfig.getRegion() + ".amazonaws.com/" + key;
    }

    private String buildObjectKey(String configuredPrefix, String effectivePrefix, String fileName) {
        String datePart = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        StringBuilder keyBuilder = new StringBuilder();

        if (StringUtils.hasText(configuredPrefix)) {
            keyBuilder.append(trimSlashes(configuredPrefix)).append('/');
        }

        keyBuilder.append(trimSlashes(effectivePrefix)).append('/').append(datePart).append('/').append(fileName);
        return keyBuilder.toString();
    }

    private String trimSlashes(String value) {
        String trimmed = value;
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }


    private String getExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            throw new IllegalArgumentException("File has no extension");
        }
        return fileName.substring(index + 1).toLowerCase();
    }
}
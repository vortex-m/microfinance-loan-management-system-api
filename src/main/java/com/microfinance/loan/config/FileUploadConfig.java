package com.microfinance.loan.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
public class FileUploadConfig {
    private String uploadDir = "upload";
    private String storageType = "local";
    private String bucketName;
    private String region;
    private String accessKey;
    private String secretKey;
    private String s3Prefix = "loan-management";

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir == null ? null : uploadDir.trim();
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType == null ? "local" : storageType.trim();
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName == null ? null : bucketName.trim();
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region == null ? null : region.trim();
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey == null ? null : accessKey.trim();
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey == null ? null : secretKey.trim();
    }

    public String getS3Prefix() {
        return s3Prefix;
    }

    public void setS3Prefix(String s3Prefix) {
        this.s3Prefix = s3Prefix == null ? null : s3Prefix.trim();
    }
}

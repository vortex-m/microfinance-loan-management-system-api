package com.microfinance.loan.common.service;

import com.microfinance.loan.config.FileUploadConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeFile_shouldPersistValidFile() throws IOException {
        FileUploadConfig config = new FileUploadConfig();
        config.setUploadDir(tempDir.toString());

        FileStorageService fileStorageService = new FileStorageService(config);
        fileStorageService.init();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.png",
                "image/png",
                "content".getBytes()
        );

        String storedFileUrl = fileStorageService.storeFile(file);
        Path storedPath = Path.of(URI.create(storedFileUrl));

        assertTrue(Files.exists(storedPath));
    }

    @Test
    void storeFile_shouldRejectInvalidExtension() {
        FileUploadConfig config = new FileUploadConfig();
        config.setUploadDir(tempDir.toString());

        FileStorageService fileStorageService = new FileStorageService(config);
        fileStorageService.init();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.exe",
                "application/octet-stream",
                "abc".getBytes()
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> fileStorageService.storeFile(file));

        assertTrue(ex.getMessage().contains("Only PDF and image files are allowed"));
    }

    @Test
    void init_shouldCreateUploadDirectoryWhenMissing() {
        Path uploadPath = tempDir.resolve("nested").resolve("uploads");
        assertFalse(Files.exists(uploadPath));

        FileUploadConfig config = new FileUploadConfig();
        config.setUploadDir(uploadPath.toString());

        FileStorageService fileStorageService = new FileStorageService(config);
        fileStorageService.init();

        assertTrue(Files.exists(uploadPath));
    }
}



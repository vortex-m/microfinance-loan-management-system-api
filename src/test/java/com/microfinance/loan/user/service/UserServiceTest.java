package com.microfinance.loan.user.service;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.FileType;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.common.service.FileStorageService;
import com.microfinance.loan.user.dto.request.KycUploadRequest;
import com.microfinance.loan.user.dto.response.KycStatusResponse;
import com.microfinance.loan.user.entity.KycDocument;
import com.microfinance.loan.user.repository.KycDocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KycDocumentRepository kycDocumentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private UserKycService userKycService;

    @Test
    void uploadKycDocument_shouldStoreS3UrlInDatabase() throws IOException {
        Users user = Users.builder().id(10L).email("test@example.com").build();
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(fileStorageService.storeFile(any(), eq("kyc/10"))).thenReturn("https://bucket.s3.ap-south-1.amazonaws.com/kyc/10/doc.png");

        ArgumentCaptor<KycDocument> captor = ArgumentCaptor.forClass(KycDocument.class);
        when(kycDocumentRepository.save(captor.capture())).thenAnswer(invocation -> {
            KycDocument doc = invocation.getArgument(0);
            doc.setId(99L);
            return doc;
        });

        KycUploadRequest request = KycUploadRequest.builder()
                .documentType(FileType.IMAGE)
                .documentNumber("ABCD1234")
                .build();

        MockMultipartFile file = new MockMultipartFile("file", "doc.png", "image/png", "content".getBytes());

        KycStatusResponse.KycDocumentItem item = userKycService.uploadKycDocument(10L, request, file);

        assertNotNull(item);
        assertEquals("https://bucket.s3.ap-south-1.amazonaws.com/kyc/10/doc.png", captor.getValue().getFileUrl());
        assertEquals("https://bucket.s3.ap-south-1.amazonaws.com/kyc/10/doc.png", item.getFileUrl());
        assertEquals(99L, item.getDocumentId());
    }
}


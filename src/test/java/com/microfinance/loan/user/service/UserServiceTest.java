package com.microfinance.loan.user.service;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.KycDocumentType;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.common.service.FileStorageService;
import com.microfinance.loan.user.dto.request.KycUploadRequest;
import com.microfinance.loan.user.dto.response.KycStatusResponse;
import com.microfinance.loan.user.entity.KycDocument;
import com.microfinance.loan.user.entity.UserProfile;
import com.microfinance.loan.user.repository.KycDocumentRepository;
import com.microfinance.loan.user.repository.UserProfileRepository;
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
    private UserProfileRepository userProfileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private UserKycService userKycService;

    @Test
    void uploadKycDocument_shouldStoreS3UrlInDatabase() throws IOException {
        Users user = Users.builder().id(10L).email("test@example.com").build();
        UserProfile profile = UserProfile.builder().id(5L).users(user).build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUsersId(10L)).thenReturn(Optional.of(profile));
        when(kycDocumentRepository.findTopByUserIdAndDocumentTypeAndIsActiveTrueOrderByVersionDesc(10L, KycDocumentType.AADHAAR))
                .thenReturn(Optional.empty());
        when(fileStorageService.storeFile(any(), eq("kyc/10/aadhaar")))
                .thenReturn("https://bucket.s3.ap-south-1.amazonaws.com/kyc/10/aadhaar/doc.png");

        ArgumentCaptor<KycDocument> captor = ArgumentCaptor.forClass(KycDocument.class);
        when(kycDocumentRepository.save(captor.capture())).thenAnswer(invocation -> {
            KycDocument doc = invocation.getArgument(0);
            if (doc.getId() == null) {
                doc.setId(99L);
            }
            return doc;
        });
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KycUploadRequest request = KycUploadRequest.builder()
                .documentType(KycDocumentType.AADHAAR)
                .documentNumber("ABCD1234")
                .build();

        MockMultipartFile file = new MockMultipartFile("file", "doc.png", "image/png", "content".getBytes());

        KycStatusResponse.KycDocumentItem item = userKycService.uploadKycDocument(10L, request, file);

        assertNotNull(item);
        assertEquals("https://bucket.s3.ap-south-1.amazonaws.com/kyc/10/aadhaar/doc.png", captor.getValue().getFileUrl());
        assertEquals("https://bucket.s3.ap-south-1.amazonaws.com/kyc/10/aadhaar/doc.png", item.getFileUrl());
        assertEquals(99L, item.getDocumentId());
        assertEquals(KycDocumentType.AADHAAR, item.getDocumentType());
    }
}

package com.microfinance.loan.officer.dto.response;

import com.microfinance.loan.common.enums.KycDocumentType;
import com.microfinance.loan.common.enums.KycStatus;
import com.microfinance.loan.common.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficerUserProfileResponse {

	private Long userId;
	private String name;
	private String email;
	private String phone;
	private UserStatus status;
	private Boolean isHome;

	private String fatherName;
	private String motherName;
	private String wifeName;
	private String husbandName;
	private LocalDate dateOfBirth;
	private String gender;
	private String occupation;
	private String maritalStatus;
	private Double monthlyIncome;

	private String street;
	private String city;
	private String state;
	private String pinCode;
	private String branchCode;
	private String branchName;
	private String regionCode;

	private String aadhaarNumber;
	private String panNumber;
	private KycStatus kycStatus;
	private List<KycItem> documents;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class KycItem {
		private Long documentId;
		private KycDocumentType documentType;
		private String documentNumber;
		private KycStatus verificationStatus;
		private String fileUrl;
		private LocalDateTime reviewedAt;
		private String officerRemarks;
		private String rejectedReason;
	}
}


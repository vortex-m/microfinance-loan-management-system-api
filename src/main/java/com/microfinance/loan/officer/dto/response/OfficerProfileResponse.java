package com.microfinance.loan.officer.dto.response;

import com.microfinance.loan.common.enums.OfficerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficerProfileResponse {
	private Long userId;
	private String name;
	private String email;
	private String phone;
	private Boolean isHome;

	private String officerCode;
	private String designation;
	private String department;
	private String branch;
	private String branchCode;

	private String fatherName;
	private String motherName;
	private LocalDate dateOfBirth;
	private String gender;
	private String maritalStatus;
	private String aadhaarNumber;
	private String panNumber;
	private String street;
	private String city;
	private String state;
	private String pincode;

	private OfficerStatus officerStatus;
}

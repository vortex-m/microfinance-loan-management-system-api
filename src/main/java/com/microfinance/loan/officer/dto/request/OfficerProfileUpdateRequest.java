package com.microfinance.loan.officer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
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
public class OfficerProfileUpdateRequest {
	@NotBlank(message = "Father name is required")
	private String fatherName;

	@NotBlank(message = "Mother name is required")
	private String motherName;

	@NotNull(message = "Date of birth is required")
	@Past(message = "Date of birth must be in the past")
	private LocalDate dateOfBirth;

	@NotBlank(message = "Gender is required")
	private String gender;

	@NotBlank(message = "Marital status is required")
	private String maritalStatus;

	@NotBlank(message = "Aadhaar number is required")
	@Pattern(regexp = "^[2-9]{1}[0-9]{11}$", message = "Invalid Aadhaar number")
	private String aadhaarNumber;

	@NotBlank(message = "PAN number is required")
	@Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format")
	private String panNumber;

	@NotBlank(message = "Street is required")
	private String street;

	@NotBlank(message = "City is required")
	private String city;

	@NotBlank(message = "State is required")
	private String state;

	@NotBlank(message = "Pincode is required")
	@Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid pincode")
	private String pincode;
}

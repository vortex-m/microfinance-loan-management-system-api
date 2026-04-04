package com.microfinance.loan.agent.dto.response;

import com.microfinance.loan.common.enums.AgentAvailability;
import com.microfinance.loan.common.enums.AgentStatus;
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
public class AgentProfileResponse {
	private Long userId;
	private String name;
	private String email;
	private String phone;
	private Boolean isHome;

	private String agentCode;
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

	private AgentStatus agentStatus;
	private AgentAvailability agentAvailability;
}

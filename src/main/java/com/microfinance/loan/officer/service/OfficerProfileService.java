package com.microfinance.loan.officer.service;

import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.common.repository.UserRepository;
import com.microfinance.loan.officer.dto.request.OfficerProfileUpdateRequest;
import com.microfinance.loan.officer.dto.response.OfficerProfileResponse;
import com.microfinance.loan.officer.entity.OfficerProfile;
import com.microfinance.loan.officer.repository.OfficerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class OfficerProfileService {

	private final OfficerProfileRepository officerProfileRepository;
	private final UserRepository userRepository;

	public OfficerProfileService(OfficerProfileRepository officerProfileRepository, UserRepository userRepository) {
		this.officerProfileRepository = officerProfileRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public OfficerProfileResponse completeOnboarding(Long userId, OfficerProfileUpdateRequest request) {
		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		if (user.getRole() != Role.OFFICER) {
			throw new IllegalArgumentException("Onboarding is allowed only for officer role");
		}

		OfficerProfile profile = officerProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("Officer profile not found for user: " + userId));

		profile.setFatherName(request.getFatherName().trim());
		profile.setMotherName(request.getMotherName().trim());
		profile.setDateOfBirth(request.getDateOfBirth());
		profile.setGender(request.getGender().trim());
		profile.setMaritalStatus(request.getMaritalStatus().trim());
		profile.setAadhaarNumber(request.getAadhaarNumber().trim());
		profile.setPanNumber(request.getPanNumber().trim().toUpperCase());
		profile.setStreet(request.getStreet().trim());
		profile.setCity(request.getCity().trim());
		profile.setState(request.getState().trim());
		profile.setPincode(request.getPincode().trim());

		user.setIsHome(isOnboardingComplete(profile));

		OfficerProfile savedProfile = officerProfileRepository.save(profile);
		userRepository.save(user);

		return mapToResponse(savedProfile, user);
	}

	private boolean isOnboardingComplete(OfficerProfile profile) {
		return StringUtils.hasText(profile.getFatherName())
				&& StringUtils.hasText(profile.getMotherName())
				&& profile.getDateOfBirth() != null
				&& StringUtils.hasText(profile.getGender())
				&& StringUtils.hasText(profile.getMaritalStatus())
				&& StringUtils.hasText(profile.getAadhaarNumber())
				&& StringUtils.hasText(profile.getPanNumber())
				&& StringUtils.hasText(profile.getStreet())
				&& StringUtils.hasText(profile.getCity())
				&& StringUtils.hasText(profile.getState())
				&& StringUtils.hasText(profile.getPincode());
	}

	private OfficerProfileResponse mapToResponse(OfficerProfile profile, Users user) {
		return OfficerProfileResponse.builder()
				.userId(user.getId())
				.name(user.getName())
				.email(user.getEmail())
				.phone(user.getPhone())
				.isHome(user.getIsHome())
				.officerCode(profile.getOfficerCode())
				.designation(profile.getDesignation())
				.department(profile.getDepartment())
				.branch(profile.getBranch())
				.branchCode(profile.getBranchCode())
				.fatherName(profile.getFatherName())
				.motherName(profile.getMotherName())
				.dateOfBirth(profile.getDateOfBirth())
				.gender(profile.getGender())
				.maritalStatus(profile.getMaritalStatus())
				.aadhaarNumber(profile.getAadhaarNumber())
				.panNumber(profile.getPanNumber())
				.street(profile.getStreet())
				.city(profile.getCity())
				.state(profile.getState())
				.pincode(profile.getPincode())
				.officerStatus(profile.getOfficerStatus())
				.build();
	}
}


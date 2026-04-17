package com.microfinance.loan.agent.service;

import com.microfinance.loan.agent.dto.request.AgentProfileUpdateRequest;
import com.microfinance.loan.common.enums.CashSettlementStatus;
import com.microfinance.loan.common.enums.AgentAvailability;
import com.microfinance.loan.agent.dto.response.AgentProfileResponse;
import com.microfinance.loan.agent.entity.AgentProfile;
import com.microfinance.loan.agent.repository.AgentProfileRepository;
import com.microfinance.loan.common.entity.Users;
import com.microfinance.loan.common.enums.Role;
import com.microfinance.loan.payment.repository.TransactionRepository;
import com.microfinance.loan.common.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AgentProfileService {

	private final AgentProfileRepository agentProfileRepository;
	private final UserRepository userRepository;
	private final TransactionRepository transactionRepository;

	public AgentProfileService(AgentProfileRepository agentProfileRepository,
							   UserRepository userRepository,
							   TransactionRepository transactionRepository) {
		this.agentProfileRepository = agentProfileRepository;
		this.userRepository = userRepository;
		this.transactionRepository = transactionRepository;
	}

	@Transactional
	public AgentProfileResponse completeOnboarding(Long userId, AgentProfileUpdateRequest request) {
		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

		if (user.getRole() != Role.AGENT) {
			throw new IllegalArgumentException("Onboarding is allowed only for agent role");
		}

		AgentProfile profile = agentProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("Agent profile not found for user: " + userId));

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

		AgentProfile savedProfile = agentProfileRepository.save(profile);
		userRepository.save(user);

		return mapToResponse(savedProfile, user);
	}

	@Transactional(readOnly = true)
	public AgentProfileResponse getMyProfile(Long userId) {
		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
		AgentProfile profile = getAgentProfile(userId);
		return mapToResponse(profile, user);
	}

	@Transactional
	public AgentProfileResponse updateAvailability(Long userId, AgentAvailability availability) {
		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
		AgentProfile profile = getAgentProfile(userId);
		profile.setAgentAvailability(availability);
		AgentProfile saved = agentProfileRepository.save(profile);
		return mapToResponse(saved, user);
	}

	@Transactional
	public AgentProfileResponse updateLocation(Long userId, Double latitude, Double longitude) {
		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
		AgentProfile profile = getAgentProfile(userId);
		profile.setLastLatitude(latitude);
		profile.setLastLongitude(longitude);
		profile.setLastLocationUpdateAt(java.time.LocalDateTime.now());
		AgentProfile saved = agentProfileRepository.save(profile);
		return mapToResponse(saved, user);
	}

	private AgentProfile getAgentProfile(Long userId) {
		return agentProfileRepository.findByUsersId(userId)
				.orElseThrow(() -> new IllegalArgumentException("Agent profile not found for user: " + userId));
	}

	private boolean isOnboardingComplete(AgentProfile profile) {
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

	private AgentProfileResponse mapToResponse(AgentProfile profile, Users user) {
		String branchName = profile.getBranchProfile() != null ? profile.getBranchProfile().getBranchName() : profile.getBranch();
		String branchCode = profile.getBranchProfile() != null ? profile.getBranchProfile().getBranchCode() : profile.getBranchCode();
		String regionCode = profile.getBranchProfile() != null ? profile.getBranchProfile().getRegionCode() : null;

		double collected = safeAmount(transactionRepository.sumCashAmountByAgentAndSettlementStatus(
				user.getId(), CashSettlementStatus.COLLECTED_UNSETTLED));
		double settled = safeAmount(transactionRepository.sumCashAmountByAgentAndSettlementStatus(
				user.getId(), CashSettlementStatus.SETTLED));

		return AgentProfileResponse.builder()
				.userId(user.getId())
				.name(user.getName())
				.email(user.getEmail())
				.phone(user.getPhone())
				.isHome(user.getIsHome())
				.agentCode(profile.getAgentCode())
				.designation(profile.getDesignation())
				.department(profile.getDepartment())
				.branch(branchName)
				.branchCode(branchCode)
				.regionCode(regionCode)
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
				.agentStatus(profile.getAgentStatus())
				.agentAvailability(profile.getAgentAvailability())
				.totalCollectedCash(round(collected + settled))
				.totalSettledCash(round(settled))
				.totalUnsettledCash(round(collected))
				.build();
	}

	private double safeAmount(Double amount) {
		return amount == null ? 0d : amount;
	}

	private double round(double value) {
		return Math.round(value * 100d) / 100d;
	}
}

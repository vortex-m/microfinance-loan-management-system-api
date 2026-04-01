package com.microfinance.loan.user.service;

import com.microfinance.loan.manager.dto.response.ManagerUserResponse;
import com.microfinance.loan.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

	private final UserProfileRepository userProfileRepository;

	public UserProfileService(UserProfileRepository userProfileRepository) {
		this.userProfileRepository = userProfileRepository;
	}

	public List<ManagerUserResponse> getAllUserProfilesForManager() {
		return userProfileRepository.findAllWithUsers().stream()
				.map(profile -> ManagerUserResponse.builder()
						.userId(profile.getUsers().getId())
						.name(profile.getUsers().getName())
						.email(profile.getUsers().getEmail())
						.role(profile.getUsers().getRole().name())
						.build())
				.collect(Collectors.toList());
	}
}

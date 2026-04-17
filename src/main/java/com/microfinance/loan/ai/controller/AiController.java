package com.microfinance.loan.ai.controller;

import com.microfinance.loan.ai.dto.CreditScoreRequest;
import com.microfinance.loan.ai.dto.CreditScoreResponse;
import com.microfinance.loan.ai.service.AiService;
import com.microfinance.loan.common.dto.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

	private final AiService aiService;

	public AiController(AiService aiService) {
		this.aiService = aiService;
	}

	@PreAuthorize("hasAnyRole('ADMIN','MANAGER','OFFICER')")
	@PostMapping("/scores/users/{userId}/refresh")
	public ApiResponse<CreditScoreResponse> refreshUserScore(@PathVariable Long userId,
															 @RequestBody(required = false) CreditScoreRequest request) {
		boolean createTask = request == null || request.getCreateFollowUpTask() == null || request.getCreateFollowUpTask();
		CreditScoreResponse response = aiService.refreshUserScore(userId, createTask);
		return ApiResponse.success("User score refreshed", response);
	}

	@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
	@PostMapping("/scores/refresh-all")
	public ApiResponse<Map<String, Object>> refreshAllScores(@RequestBody(required = false) CreditScoreRequest request) {
		boolean createTask = request == null || request.getCreateFollowUpTask() == null || request.getCreateFollowUpTask();
		int refreshed = aiService.refreshScoresForAllUsers(createTask);
		return ApiResponse.success("All user scores refreshed", Map.of("refreshedUsers", refreshed));
	}

}


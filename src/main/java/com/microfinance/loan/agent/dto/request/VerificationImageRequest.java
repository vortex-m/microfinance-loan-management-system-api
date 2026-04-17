package com.microfinance.loan.agent.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationImageRequest {

	private String imageTag;
	private String description;
	private Double captureLatitude;
	private Double captureLongitude;
}

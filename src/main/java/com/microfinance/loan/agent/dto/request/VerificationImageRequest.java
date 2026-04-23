package com.microfinance.loan.agent.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

	@NotBlank(message = "Image tag is required")
	private String imageTag;
	@NotBlank(message = "Image description is required")
	private String description;
	@NotNull(message = "Capture latitude is required")
	private Double captureLatitude;
	@NotNull(message = "Capture longitude is required")
	private Double captureLongitude;
}

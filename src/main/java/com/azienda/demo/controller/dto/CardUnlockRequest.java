package com.azienda.demo.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CardUnlockRequest(
		@NotBlank(message = "Card number is required")
		@Pattern(regexp = "^\\d{16}$", message = "Card number must be 16 digits")
		String cardNumber
) {}

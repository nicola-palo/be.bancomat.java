package com.azienda.demo.api.dto;

public record CardValidateResponse(
		boolean valid,
		boolean active,
		String maskedNumber
) {
}

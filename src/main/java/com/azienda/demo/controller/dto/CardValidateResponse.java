package com.azienda.demo.controller.dto;

public record CardValidateResponse(
		boolean valid,
		boolean active,
		String maskedNumber
) {
}

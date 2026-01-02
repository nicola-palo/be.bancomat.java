package com.azienda.demo.api.dto;

public record CardLoginResponse(
		String token,
		AccountDto account
) {
}

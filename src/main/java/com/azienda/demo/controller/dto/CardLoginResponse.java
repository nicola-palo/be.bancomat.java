package com.azienda.demo.controller.dto;

public record CardLoginResponse(
		String token,
		AccountDto account
) {
}

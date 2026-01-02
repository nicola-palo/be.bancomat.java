package com.azienda.demo.api.dto;

public record UserDto(
		long id,
		String firstName,
		String lastName,
		String fiscalCode
) {
}

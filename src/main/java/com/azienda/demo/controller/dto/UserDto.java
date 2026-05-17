package com.azienda.demo.controller.dto;

public record UserDto(
		long id,
		String firstName,
		String lastName,
		String fiscalCode
) {
}

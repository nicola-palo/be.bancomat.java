package com.azienda.demo.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountDto(
		long id,
		String accountNumber,
		BigDecimal balance,
		String currency,
		Instant createdAt,
		UserDto owner
) {
}

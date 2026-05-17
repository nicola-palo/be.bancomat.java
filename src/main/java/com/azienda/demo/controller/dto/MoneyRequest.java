package com.azienda.demo.controller.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record MoneyRequest(
		@NotNull
		@DecimalMin(value = "0.01")
		@Digits(integer = 17, fraction = 2)
		BigDecimal amount
) {
}

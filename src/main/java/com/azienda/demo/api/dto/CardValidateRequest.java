package com.azienda.demo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CardValidateRequest(
		@NotBlank(message = "Il numero carta è obbligatorio")
		@Size(min = 16, max = 16, message = "Il numero carta deve essere di 16 cifre")
		@Pattern(regexp = "\\d{16}", message = "Il numero carta deve contenere solo cifre")
		String cardNumber
) {
}

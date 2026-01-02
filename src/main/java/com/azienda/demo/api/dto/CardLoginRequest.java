package com.azienda.demo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CardLoginRequest(
		@NotBlank(message = "Il numero carta è obbligatorio")
		@Size(min = 16, max = 16, message = "Il numero carta deve essere di 16 cifre")
		@Pattern(regexp = "\\d{16}", message = "Il numero carta deve contenere solo cifre")
		String cardNumber,

		@NotBlank(message = "Il PIN è obbligatorio")
		@Size(min = 4, max = 6, message = "Il PIN deve essere di 4-6 cifre")
		@Pattern(regexp = "\\d{4,6}", message = "Il PIN deve contenere solo cifre")
		String pin
) {
}

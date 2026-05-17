package com.azienda.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.azienda.demo.controller.dto.CardUnlockRequest;
import com.azienda.demo.controller.dto.CardUnlockResponse;
import com.azienda.demo.service.AuthService;

import jakarta.validation.Valid;

/**
 * Controller per endpoint interni (BE.CHAT -> BE).
 * Protetto da API key tramite ApiKeyFilter.
 */
@RestController
@RequestMapping(value = "/api/internal", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class InternalController {
	
	private final AuthService authService;

	public InternalController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping(value = "/unlock-card", consumes = MediaType.APPLICATION_JSON_VALUE)
	public CardUnlockResponse unlockCard(@Valid @RequestBody CardUnlockRequest request) {
		boolean unlocked = authService.unlockCard(request.cardNumber());
		if (unlocked) {
			return new CardUnlockResponse(true, "Carta sbloccata con successo. Puoi ora effettuare il login.");
		} else {
			return new CardUnlockResponse(false, "Carta non trovata.");
		}
	}
}

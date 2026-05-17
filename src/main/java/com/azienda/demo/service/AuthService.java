package com.azienda.demo.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.azienda.demo.config.JwtService;
import com.azienda.demo.model.Card;
import com.azienda.demo.repository.CardRepository;
import com.azienda.demo.service.exception.ForbiddenException;
import com.azienda.demo.service.exception.UnauthorizedException;

@Service
public class AuthService {
	private static final Logger log = LoggerFactory.getLogger(AuthService.class);
	
	private final CardRepository cardRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	@Value("${atm.security.max-pin-attempts:3}")
	private int maxPinAttempts;

	public AuthService(CardRepository cardRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.cardRepository = cardRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional(noRollbackFor = {UnauthorizedException.class, ForbiddenException.class})
	public LoginResult loginWithCard(String cardNumber, String pin) {
		Card card = cardRepository.findByCardNumberWithAccountOwner(cardNumber)
				.orElseThrow(() -> new UnauthorizedException("Card not found"));

		if (!card.isActive()) {
			throw new ForbiddenException("Card is blocked/inactive");
		}

		if (!passwordEncoder.matches(pin, card.getPinHash())) {
			// Incrementa tentativi falliti
			card.incrementFailedAttempts();
			int remaining = maxPinAttempts - card.getFailedAttempts();
			
			String maskedCard = cardNumber.substring(0, 4) + "****" + cardNumber.substring(12);
			log.info("PIN errato per carta {}. Tentativi falliti: {}, rimanenti: {}", 
				maskedCard, card.getFailedAttempts(), remaining);

			if (card.getFailedAttempts() >= maxPinAttempts) {
				// Blocca la carta dopo troppi tentativi
				card.setActive(false);
				Card saved = cardRepository.saveAndFlush(card);
				log.info("Carta bloccata. failedAttempts dopo save: {}", saved.getFailedAttempts());
				throw new ForbiddenException("Card blocked after too many failed attempts");
			}

			Card saved = cardRepository.saveAndFlush(card);
			log.info("Card salvata. failedAttempts dopo save: {}", saved.getFailedAttempts());
			throw new UnauthorizedException("Invalid PIN. Attempts remaining: " + remaining);
		}

		// Login riuscito: resetta i tentativi falliti
		if (card.getFailedAttempts() > 0) {
			card.resetFailedAttempts();
			cardRepository.save(card);
		}

		long userId = card.getAccount().getOwner().getId();
		String token = jwtService.issueToken(userId, Map.of(
				"accountId", card.getAccount().getId(),
				"cardNumber", card.getCardNumber()
		));

		return new LoginResult(token, card);
	}

	public record LoginResult(String token, Card card) {
	}

	@Transactional
	public boolean unlockCard(String cardNumber) {
		return cardRepository.findByCardNumber(cardNumber)
				.map(card -> {
					card.setActive(true);
					card.resetFailedAttempts();
					cardRepository.save(card);
					return true;
				})
				.orElse(false);
	}
}

package com.azienda.demo.api;

import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.azienda.demo.api.dto.AccountDto;
import com.azienda.demo.api.dto.CardLoginRequest;
import com.azienda.demo.api.dto.CardLoginResponse;
import com.azienda.demo.api.dto.CardValidateRequest;
import com.azienda.demo.api.dto.CardValidateResponse;
import com.azienda.demo.api.dto.UserDto;
import com.azienda.demo.domain.Account;
import com.azienda.demo.domain.Card;
import com.azienda.demo.domain.User;
import com.azienda.demo.repo.CardRepository;
import com.azienda.demo.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class AuthController {
	private final AuthService authService;
	private final CardRepository cardRepository;

	public AuthController(AuthService authService, CardRepository cardRepository) {
		this.authService = authService;
		this.cardRepository = cardRepository;
	}

	@PostMapping(value = "/validate-card", consumes = MediaType.APPLICATION_JSON_VALUE)
	public CardValidateResponse validateCard(@Valid @RequestBody CardValidateRequest request) {
		Optional<Card> cardOpt = cardRepository.findByCardNumber(request.cardNumber());

		if (cardOpt.isEmpty()) {
			return new CardValidateResponse(false, false, null);
		}

		Card card = cardOpt.get();
		String masked = request.cardNumber().substring(0, 4) + " •••• •••• " + request.cardNumber().substring(12);
		return new CardValidateResponse(true, card.isActive(), masked);
	}

	@PostMapping(value = "/card-login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public CardLoginResponse cardLogin(@Valid @RequestBody CardLoginRequest request) {
		AuthService.LoginResult result = authService.loginWithCard(request.cardNumber(), request.pin());
		Card card = result.card();
		Account account = card.getAccount();
		User owner = account.getOwner();

		UserDto ownerDto = new UserDto(owner.getId(), owner.getFirstName(), owner.getLastName(), owner.getFiscalCode());
		AccountDto accountDto = new AccountDto(account.getId(), account.getAccountNumber(), account.getBalance(),
				account.getCurrency(), account.getCreatedAt(), ownerDto);
		return new CardLoginResponse(result.token(), accountDto);
	}
}

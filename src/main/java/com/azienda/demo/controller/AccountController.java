package com.azienda.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.azienda.demo.controller.dto.AccountDto;
import com.azienda.demo.controller.dto.MoneyRequest;
import com.azienda.demo.controller.dto.UserDto;
import com.azienda.demo.model.Account;
import com.azienda.demo.model.User;
import com.azienda.demo.service.AccountService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping(value = "/api/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {
	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@GetMapping("/{accountId}")
	public AccountDto getAccount(@PathVariable long accountId, @AuthenticationPrincipal Jwt jwt) {
		long userId = Long.parseLong(jwt.getSubject());
		Account account = accountService.getAccountForUser(accountId, userId);
		return toDto(account);
	}

	@PostMapping(value = "/{accountId}/deposit", consumes = MediaType.APPLICATION_JSON_VALUE)
	public AccountDto deposit(@PathVariable long accountId, @Valid @RequestBody MoneyRequest req, @AuthenticationPrincipal Jwt jwt) {
		long userId = Long.parseLong(jwt.getSubject());
		Account updated = accountService.deposit(accountId, userId, req.amount());
		return toDto(updated);
	}

	@PostMapping(value = "/{accountId}/withdraw", consumes = MediaType.APPLICATION_JSON_VALUE)
	public AccountDto withdraw(@PathVariable long accountId, @Valid @RequestBody MoneyRequest req, @AuthenticationPrincipal Jwt jwt) {
		long userId = Long.parseLong(jwt.getSubject());
		Account updated = accountService.withdraw(accountId, userId, req.amount());
		return toDto(updated);
	}

	private static AccountDto toDto(Account account) {
		User owner = account.getOwner();
		UserDto ownerDto = new UserDto(owner.getId(), owner.getFirstName(), owner.getLastName(), owner.getFiscalCode());
		return new AccountDto(account.getId(), account.getAccountNumber(), account.getBalance(), account.getCurrency(),
				account.getCreatedAt(), ownerDto);
	}
}

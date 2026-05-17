package com.azienda.demo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.azienda.demo.model.Account;
import com.azienda.demo.repository.AccountRepository;
import com.azienda.demo.service.exception.BadRequestException;
import com.azienda.demo.service.exception.ForbiddenException;
import com.azienda.demo.service.exception.NotFoundException;

@Service
public class AccountService {
	private final AccountRepository accountRepository;

	public AccountService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Transactional(readOnly = true)
	public Account getAccountForUser(long accountId, long userId) {
		Account account = accountRepository.findById(accountId)
				.orElseThrow(() -> new NotFoundException("Account not found"));
		enforceOwner(account, userId);
		return account;
	}

	@Transactional
	public Account deposit(long accountId, long userId, BigDecimal amount) {
		BigDecimal normalized = normalizeAmount(amount);
		Account account = accountRepository.findByIdForUpdate(accountId)
				.orElseThrow(() -> new NotFoundException("Account not found"));
		enforceOwner(account, userId);

		account.setBalance(account.getBalance().add(normalized));
		return account;
	}

	@Transactional
	public Account withdraw(long accountId, long userId, BigDecimal amount) {
		BigDecimal normalized = normalizeAmount(amount);
		Account account = accountRepository.findByIdForUpdate(accountId)
				.orElseThrow(() -> new NotFoundException("Account not found"));
		enforceOwner(account, userId);

		if (account.getBalance().compareTo(normalized) < 0) {
			throw new BadRequestException("Insufficient funds");
		}

		account.setBalance(account.getBalance().subtract(normalized));
		return account;
	}

	private static void enforceOwner(Account account, long userId) {
		if (account.getOwner() == null || account.getOwner().getId() == null || account.getOwner().getId() != userId) {
			throw new ForbiddenException("Account does not belong to authenticated user");
		}
	}

	private static BigDecimal normalizeAmount(BigDecimal amount) {
		if (amount == null) {
			throw new BadRequestException("Amount is required");
		}
		BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
		if (normalized.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BadRequestException("Amount must be positive");
		}
		return normalized;
	}
}

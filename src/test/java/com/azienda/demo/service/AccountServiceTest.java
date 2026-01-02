package com.azienda.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.azienda.demo.domain.Account;
import com.azienda.demo.domain.User;
import com.azienda.demo.repo.AccountRepository;
import com.azienda.demo.repo.UserRepository;
import com.azienda.demo.service.exception.BadRequestException;
import com.azienda.demo.service.exception.ForbiddenException;
import com.azienda.demo.service.exception.NotFoundException;

@SpringBootTest
@Transactional
class AccountServiceTest {

	@Autowired
	private AccountService accountService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AccountRepository accountRepository;

	private User testUser;
	private User otherUser;
	private Account testAccount;

	@BeforeEach
	void setUp() {
		// Utente principale
		testUser = new User();
		testUser.setFirstName("Luigi");
		testUser.setLastName("Verdi");
		testUser.setFiscalCode("VRDLGU90A01H501X");

		testAccount = new Account();
		testAccount.setAccountNumber("IT00X000000000000000000088");
		testAccount.setBalance(new BigDecimal("1000.00"));
		testAccount.setCurrency("EUR");
		testAccount.setCreatedAt(Instant.now());
		testAccount.setOwner(testUser);
		testUser.getAccounts().add(testAccount);

		userRepository.save(testUser);

		// Altro utente (per test autorizzazione)
		otherUser = new User();
		otherUser.setFirstName("Paolo");
		otherUser.setLastName("Bianchi");
		otherUser.setFiscalCode("BNCPLA85M01H501Y");
		userRepository.save(otherUser);
	}

	@Nested
	@DisplayName("Recupero conto")
	class GetAccountTests {

		@Test
		@DisplayName("Recupera conto dell'utente autenticato")
		void getAccount_forOwner_returnsAccount() {
			Account result = accountService.getAccountForUser(testAccount.getId(), testUser.getId());

			assertThat(result).isNotNull();
			assertThat(result.getAccountNumber()).isEqualTo("IT00X000000000000000000088");
			assertThat(result.getBalance()).isEqualByComparingTo("1000.00");
		}

		@Test
		@DisplayName("Recupero conto di altro utente lancia ForbiddenException")
		void getAccount_forOtherUser_throwsForbidden() {
			assertThatThrownBy(() -> accountService.getAccountForUser(testAccount.getId(), otherUser.getId()))
					.isInstanceOf(ForbiddenException.class)
					.hasMessage("Account does not belong to authenticated user");
		}

		@Test
		@DisplayName("Recupero conto inesistente lancia NotFoundException")
		void getAccount_notFound_throwsNotFound() {
			assertThatThrownBy(() -> accountService.getAccountForUser(99999L, testUser.getId()))
					.isInstanceOf(NotFoundException.class)
					.hasMessage("Account not found");
		}
	}

	@Nested
	@DisplayName("Deposito")
	class DepositTests {

		@Test
		@DisplayName("Deposito valido incrementa il saldo")
		void deposit_validAmount_increasesBalance() {
			Account result = accountService.deposit(testAccount.getId(), testUser.getId(), new BigDecimal("250.00"));

			assertThat(result.getBalance()).isEqualByComparingTo("1250.00");
		}

		@Test
		@DisplayName("Deposito con decimali viene arrotondato a 2 cifre")
		void deposit_withDecimals_roundsToTwoPlaces() {
			Account result = accountService.deposit(testAccount.getId(), testUser.getId(), new BigDecimal("100.555"));

			// 100.555 arrotondato = 100.56
			assertThat(result.getBalance()).isEqualByComparingTo("1100.56");
		}

		@Test
		@DisplayName("Deposito di importo zero lancia BadRequestException")
		void deposit_zeroAmount_throwsBadRequest() {
			assertThatThrownBy(() -> accountService.deposit(testAccount.getId(), testUser.getId(), BigDecimal.ZERO))
					.isInstanceOf(BadRequestException.class)
					.hasMessage("Amount must be positive");
		}

		@Test
		@DisplayName("Deposito di importo negativo lancia BadRequestException")
		void deposit_negativeAmount_throwsBadRequest() {
			assertThatThrownBy(() -> accountService.deposit(testAccount.getId(), testUser.getId(), new BigDecimal("-50")))
					.isInstanceOf(BadRequestException.class)
					.hasMessage("Amount must be positive");
		}

		@Test
		@DisplayName("Deposito su conto altrui lancia ForbiddenException")
		void deposit_otherUserAccount_throwsForbidden() {
			assertThatThrownBy(() -> accountService.deposit(testAccount.getId(), otherUser.getId(), new BigDecimal("100")))
					.isInstanceOf(ForbiddenException.class);
		}

		@Test
		@DisplayName("Deposito con amount null lancia BadRequestException")
		void deposit_nullAmount_throwsBadRequest() {
			assertThatThrownBy(() -> accountService.deposit(testAccount.getId(), testUser.getId(), null))
					.isInstanceOf(BadRequestException.class)
					.hasMessage("Amount is required");
		}
	}

	@Nested
	@DisplayName("Prelievo")
	class WithdrawTests {

		@Test
		@DisplayName("Prelievo valido decrementa il saldo")
		void withdraw_validAmount_decreasesBalance() {
			Account result = accountService.withdraw(testAccount.getId(), testUser.getId(), new BigDecimal("300.00"));

			assertThat(result.getBalance()).isEqualByComparingTo("700.00");
		}

		@Test
		@DisplayName("Prelievo dell'intero saldo azzera il conto")
		void withdraw_entireBalance_zeroes() {
			Account result = accountService.withdraw(testAccount.getId(), testUser.getId(), new BigDecimal("1000.00"));

			assertThat(result.getBalance()).isEqualByComparingTo("0.00");
		}

		@Test
		@DisplayName("Prelievo superiore al saldo lancia BadRequestException")
		void withdraw_insufficientFunds_throwsBadRequest() {
			assertThatThrownBy(() -> accountService.withdraw(testAccount.getId(), testUser.getId(), new BigDecimal("1500.00")))
					.isInstanceOf(BadRequestException.class)
					.hasMessage("Insufficient funds");
		}

		@Test
		@DisplayName("Prelievo di importo zero lancia BadRequestException")
		void withdraw_zeroAmount_throwsBadRequest() {
			assertThatThrownBy(() -> accountService.withdraw(testAccount.getId(), testUser.getId(), BigDecimal.ZERO))
					.isInstanceOf(BadRequestException.class)
					.hasMessage("Amount must be positive");
		}

		@Test
		@DisplayName("Prelievo di importo negativo lancia BadRequestException")
		void withdraw_negativeAmount_throwsBadRequest() {
			assertThatThrownBy(() -> accountService.withdraw(testAccount.getId(), testUser.getId(), new BigDecimal("-100")))
					.isInstanceOf(BadRequestException.class)
					.hasMessage("Amount must be positive");
		}

		@Test
		@DisplayName("Prelievo da conto altrui lancia ForbiddenException")
		void withdraw_otherUserAccount_throwsForbidden() {
			assertThatThrownBy(() -> accountService.withdraw(testAccount.getId(), otherUser.getId(), new BigDecimal("50")))
					.isInstanceOf(ForbiddenException.class);
		}
	}

	@Nested
	@DisplayName("Operazioni concorrenti")
	class ConcurrencyTests {

		@Test
		@DisplayName("Depositi multipli aggiornano correttamente il saldo")
		void multipleDeposits_correctBalance() {
			accountService.deposit(testAccount.getId(), testUser.getId(), new BigDecimal("100"));
			accountService.deposit(testAccount.getId(), testUser.getId(), new BigDecimal("200"));
			Account result = accountService.deposit(testAccount.getId(), testUser.getId(), new BigDecimal("50"));

			assertThat(result.getBalance()).isEqualByComparingTo("1350.00");
		}

		@Test
		@DisplayName("Mix depositi e prelievi aggiorna correttamente il saldo")
		void mixedOperations_correctBalance() {
			accountService.deposit(testAccount.getId(), testUser.getId(), new BigDecimal("500"));   // 1500
			accountService.withdraw(testAccount.getId(), testUser.getId(), new BigDecimal("200"));  // 1300
			Account result = accountService.deposit(testAccount.getId(), testUser.getId(), new BigDecimal("100")); // 1400

			assertThat(result.getBalance()).isEqualByComparingTo("1400.00");
		}
	}
}

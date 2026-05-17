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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.azienda.demo.model.Account;
import com.azienda.demo.model.Card;
import com.azienda.demo.model.User;
import com.azienda.demo.repository.CardRepository;
import com.azienda.demo.repository.UserRepository;
import com.azienda.demo.service.exception.ForbiddenException;
import com.azienda.demo.service.exception.UnauthorizedException;

@SpringBootTest
@Transactional
class AuthServiceTest {

	@Autowired
	private AuthService authService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CardRepository cardRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private User testUser;
	private Account testAccount;
	private Card testCard;

	@BeforeEach
	void setUp() {
		// Crea utente di test
		testUser = new User();
		testUser.setFirstName("Mario");
		testUser.setLastName("Rossi");
		testUser.setFiscalCode("RSSMRA85M01H501Z");

		// Crea conto di test
		testAccount = new Account();
		testAccount.setAccountNumber("IT00X000000000000000000099");
		testAccount.setBalance(new BigDecimal("500.00"));
		testAccount.setCurrency("EUR");
		testAccount.setCreatedAt(Instant.now());
		testAccount.setOwner(testUser);
		testUser.getAccounts().add(testAccount);

		userRepository.save(testUser);

		// Crea carta di test
		testCard = new Card();
		testCard.setCardNumber("9999888877776666");
		testCard.setPinHash(passwordEncoder.encode("5678"));
		testCard.setActive(true);
		testCard.setAccount(testAccount);
		cardRepository.save(testCard);
	}

	@Nested
	@DisplayName("Login con carta")
	class LoginWithCardTests {

		@Test
		@DisplayName("Login con credenziali valide restituisce token e carta")
		void loginWithValidCredentials_returnsTokenAndCard() {
			AuthService.LoginResult result = authService.loginWithCard("9999888877776666", "5678");

			assertThat(result).isNotNull();
			assertThat(result.token()).isNotBlank();
			assertThat(result.card()).isNotNull();
			assertThat(result.card().getCardNumber()).isEqualTo("9999888877776666");
			assertThat(result.card().getAccount()).isNotNull();
			assertThat(result.card().getAccount().getOwner()).isNotNull();
		}

		@Test
		@DisplayName("Login con carta inesistente lancia UnauthorizedException")
		void loginWithInvalidCard_throwsUnauthorized() {
			assertThatThrownBy(() -> authService.loginWithCard("0000000000000000", "5678"))
					.isInstanceOf(UnauthorizedException.class)
					.hasMessage("Card not found");
		}

		@Test
		@DisplayName("Login con PIN errato mostra tentativi rimanenti")
		void loginWithInvalidPin_showsRemainingAttempts() {
			assertThatThrownBy(() -> authService.loginWithCard("9999888877776666", "0000"))
					.isInstanceOf(UnauthorizedException.class)
					.hasMessageContaining("Invalid PIN")
					.hasMessageContaining("Attempts remaining: 2");
		}

		@Test
		@DisplayName("Login con carta disattivata lancia ForbiddenException")
		void loginWithInactiveCard_throwsForbidden() {
			testCard.setActive(false);
			cardRepository.save(testCard);

			assertThatThrownBy(() -> authService.loginWithCard("9999888877776666", "5678"))
					.isInstanceOf(ForbiddenException.class)
					.hasMessage("Card is blocked/inactive");
		}

		@Test
		@DisplayName("Carta bloccata dopo 3 tentativi PIN errati")
		void cardBlockedAfterThreeFailedAttempts() {
			// Primo tentativo
			assertThatThrownBy(() -> authService.loginWithCard("9999888877776666", "0000"))
					.isInstanceOf(UnauthorizedException.class)
					.hasMessageContaining("Attempts remaining: 2");

			// Secondo tentativo
			assertThatThrownBy(() -> authService.loginWithCard("9999888877776666", "0000"))
					.isInstanceOf(UnauthorizedException.class)
					.hasMessageContaining("Attempts remaining: 1");

			// Terzo tentativo - carta bloccata
			assertThatThrownBy(() -> authService.loginWithCard("9999888877776666", "0000"))
					.isInstanceOf(ForbiddenException.class)
					.hasMessage("Card blocked after too many failed attempts");

			// Ulteriore tentativo - carta già bloccata
			assertThatThrownBy(() -> authService.loginWithCard("9999888877776666", "5678"))
					.isInstanceOf(ForbiddenException.class)
					.hasMessage("Card is blocked/inactive");
		}

		@Test
		@DisplayName("Login riuscito resetta i tentativi falliti")
		void successfulLoginResetsFailedAttempts() {
			// Un tentativo fallito
			assertThatThrownBy(() -> authService.loginWithCard("9999888877776666", "0000"))
					.isInstanceOf(UnauthorizedException.class);

			// Login riuscito
			AuthService.LoginResult result = authService.loginWithCard("9999888877776666", "5678");
			assertThat(result).isNotNull();

			// Verifica che i tentativi siano stati resettati
			Card card = cardRepository.findByCardNumberWithAccountOwner("9999888877776666").orElseThrow();
			assertThat(card.getFailedAttempts()).isZero();
		}

		@Test
		@DisplayName("Token JWT contiene l'ID utente come subject")
		void loginToken_containsUserIdAsSubject() {
			AuthService.LoginResult result = authService.loginWithCard("9999888877776666", "5678");

			// Il token è un JWT valido con 3 parti
			String[] parts = result.token().split("\\.");
			assertThat(parts).hasSize(3);
		}
	}
}

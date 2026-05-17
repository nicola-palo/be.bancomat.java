package com.azienda.demo.data;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.azienda.demo.model.Account;
import com.azienda.demo.model.Card;
import com.azienda.demo.model.User;
import com.azienda.demo.repository.CardRepository;
import com.azienda.demo.repository.UserRepository;

@Component
@ConditionalOnProperty(prefix = "atm.seed", name = "enabled", havingValue = "true")
public class DemoSeeder implements CommandLineRunner {
	private final UserRepository userRepository;
	private final CardRepository cardRepository;
	private final PasswordEncoder passwordEncoder;

	public DemoSeeder(UserRepository userRepository, CardRepository cardRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.cardRepository = cardRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void run(String... args) {
		if (cardRepository.findByCardNumber("1111222233334444").isPresent()) {
			return;
		}

		User user = new User();
		user.setFirstName("Nicola");
		user.setLastName("Rossi");
		user.setFiscalCode("RSSNCL00A00H501Z");

		Account account = new Account();
		account.setAccountNumber("IT00X000000000000000000001");
		account.setBalance(new BigDecimal("1000.00"));
		account.setCurrency("EUR");
		account.setCreatedAt(Instant.now());
		account.setOwner(user);
		user.getAccounts().add(account);

		User saved = userRepository.save(user);

		Card card = new Card();
		card.setCardNumber("1111222233334444");
		card.setPinHash(passwordEncoder.encode("1234"));
		card.setActive(true);
		card.setAccount(saved.getAccounts().getFirst());
		cardRepository.save(card);
	}
}

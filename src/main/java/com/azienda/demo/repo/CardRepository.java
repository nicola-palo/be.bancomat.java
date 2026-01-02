package com.azienda.demo.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.azienda.demo.domain.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
	Optional<Card> findByCardNumber(String cardNumber);

	@Query("select c from Card c join fetch c.account a join fetch a.owner where c.cardNumber = :cardNumber")
	Optional<Card> findByCardNumberWithAccountOwner(@Param("cardNumber") String cardNumber);
}

package com.azienda.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.azienda.demo.model.Account;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {
	Optional<Account> findByAccountNumber(String accountNumber);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Account a join fetch a.owner where a.id = :id")
	Optional<Account> findByIdForUpdate(@Param("id") long id);
}

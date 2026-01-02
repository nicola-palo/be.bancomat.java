package com.azienda.demo.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.azienda.demo.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByFiscalCode(String fiscalCode);
}

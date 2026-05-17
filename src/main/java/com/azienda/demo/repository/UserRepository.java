package com.azienda.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.azienda.demo.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByFiscalCode(String fiscalCode);
}

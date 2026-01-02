package com.azienda.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "atm.jwt")
public record JwtProperties(
		String secret,
		String issuer,
		long ttlSeconds
) {
}

package com.azienda.demo.config;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Service
public class JwtService {
	private final JwtProperties props;
	private final JwtEncoder encoder;

	public JwtService(JwtProperties props) {
		this.props = props;
		SecretKey key = new SecretKeySpec(props.secret().getBytes(), "HmacSHA256");
		this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
	}

	public String issueToken(long userId, Map<String, Object> extraClaims) {
		Instant now = Instant.now();
		JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
				.issuer(props.issuer())
				.issuedAt(now)
				.expiresAt(now.plus(props.ttlSeconds(), ChronoUnit.SECONDS))
				.subject(Long.toString(userId));

		extraClaims.forEach(claims::claim);

		JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
		Jwt jwt = encoder.encode(JwtEncoderParameters.from(headers, claims.build()));
		return jwt.getTokenValue();
	}
}

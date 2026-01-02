package com.azienda.demo.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtro per proteggere gli endpoint interni con API key.
 * Usato per la comunicazione sicura tra BE.CHAT e BE.
 */
@Component
@Order(1)
public class ApiKeyFilter extends OncePerRequestFilter {

	private static final String API_KEY_HEADER = "X-API-Key";

	@Value("${atm.internal.api-key:default-internal-key-change-in-prod}")
	private String apiKey;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String path = request.getRequestURI();
		
		// Applica solo agli endpoint /api/internal/**
		if (path.startsWith("/api/internal/")) {
			String providedKey = request.getHeader(API_KEY_HEADER);
			
			if (providedKey == null || !providedKey.equals(apiKey)) {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				response.setContentType("application/json");
				response.getWriter().write("{\"message\":\"Invalid or missing API key\"}");
				return;
			}
		}
		
		filterChain.doFilter(request, response);
	}
}

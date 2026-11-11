package com.bytestorm.waygo.infrastructure.config.security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.bytestorm.waygo.domain.interfaces.TokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthFilter extends OncePerRequestFilter {

	private static final String PREFIXO_BEARER = "Bearer ";

	private final TokenProvider tokenProvider;
	private final UserDetailsService userDetailsService;

	public AuthFilter(TokenProvider tokenProvider, UserDetailsService userDetailsService) {
		this.tokenProvider = tokenProvider;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		if (SecurityContextHolder.getContext().getAuthentication() == null) {
			extrairToken(request)
					.flatMap(tokenProvider::validarToken)
					.flatMap(this::carregarUsuario)
					.ifPresent(usuario -> autenticar(usuario, request));
		}
		filterChain.doFilter(request, response);
	}

	private Optional<String> extrairToken(HttpServletRequest request) {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.regionMatches(true, 0, PREFIXO_BEARER, 0, PREFIXO_BEARER.length())) {
			return Optional.empty();
		}
		String token = header.substring(PREFIXO_BEARER.length()).trim();
		return token.isEmpty() ? Optional.empty() : Optional.of(token);
	}

	private Optional<UserDetails> carregarUsuario(String email) {
		try {
			return Optional.of(userDetailsService.loadUserByUsername(email));
		} catch (UsernameNotFoundException e) {
			return Optional.empty();
		}
	}

	private void autenticar(UserDetails usuario, HttpServletRequest request) {
		UsernamePasswordAuthenticationToken autenticacao = UsernamePasswordAuthenticationToken
				.authenticated(usuario, null, usuario.getAuthorities());
		autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

		SecurityContext contexto = SecurityContextHolder.createEmptyContext();
		contexto.setAuthentication(autenticacao);
		SecurityContextHolder.setContext(contexto);
	}

}

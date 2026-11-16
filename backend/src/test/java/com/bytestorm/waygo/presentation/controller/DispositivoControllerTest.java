package com.bytestorm.waygo.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bytestorm.waygo.application.dto.request.DispositivoRequest;
import com.bytestorm.waygo.application.service.DispositivoService;
import com.bytestorm.waygo.domain.entity.Usuario;
import com.bytestorm.waygo.domain.enums.Plataforma;
import com.bytestorm.waygo.domain.enums.Role;
import com.bytestorm.waygo.domain.interfaces.TokenProvider;
import com.bytestorm.waygo.infrastructure.config.security.AuthFilter;
import com.bytestorm.waygo.infrastructure.config.security.SecurityConfig;
import com.bytestorm.waygo.infrastructure.config.security.UsuarioAutenticado;

/** Camada web com o SecurityConfig real; o login passa pelo AuthFilter com o token simulado. */
@WebMvcTest(DispositivoController.class)
@Import({ SecurityConfig.class, DispositivoControllerTest.Relogio.class })
class DispositivoControllerTest {

	@TestConfiguration
	static class Relogio {
		@Bean
		Clock clock() {
			return Clock.fixed(Instant.parse("2026-11-16T12:00:00Z"), ZoneOffset.UTC);
		}

		/** O AuthFilter deve rodar só dentro da cadeia do Spring Security, não também como filtro solto. */
		@Bean
		FilterRegistrationBean<AuthFilter> authFilterSoNaCadeiaDeSeguranca(AuthFilter authFilter) {
			FilterRegistrationBean<AuthFilter> registro = new FilterRegistrationBean<>(authFilter);
			registro.setEnabled(false);
			return registro;
		}
	}

	private static final UUID ANA = UUID.randomUUID();
	private static final String BEARER = "Bearer token-da-ana";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private DispositivoService dispositivoService;

	@MockitoBean
	private TokenProvider tokenProvider;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@BeforeEach
	void logarAna() {
		given(tokenProvider.validarToken("token-da-ana")).willReturn(Optional.of("ana@email.com"));
		given(userDetailsService.loadUserByUsername("ana@email.com")).willReturn(new UsuarioAutenticado(
				Usuario.reconstruir(ANA, "ana@email.com", "hash", Set.of(Role.USUARIO), LocalDateTime.now())));
	}

	@Test
	void registrarDevolve204ParaOUsuarioLogado() throws Exception {
		mockMvc.perform(post("/api/v1/dispositivos").header(HttpHeaders.AUTHORIZATION, BEARER)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"token": "fcm-123", "plataforma": "ANDROID", "alertasStatus": false}
						"""))
				.andExpect(status().isNoContent());

		verify(dispositivoService).registrar(ANA, new DispositivoRequest("fcm-123", Plataforma.ANDROID, false));
	}

	@Test
	void registrarSemTokenDoAparelhoDevolve400() throws Exception {
		mockMvc.perform(post("/api/v1/dispositivos").header(HttpHeaders.AUTHORIZATION, BEARER)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"token": "", "plataforma": "ANDROID"}
						"""))
				.andExpect(status().isBadRequest());

		verifyNoInteractions(dispositivoService);
	}

	@Test
	void semLoginNaoRegistra() throws Exception {
		mockMvc.perform(post("/api/v1/dispositivos").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"token": "fcm-123", "plataforma": "ANDROID"}
						"""))
				.andExpect(status().isUnauthorized());

		verifyNoInteractions(dispositivoService);
	}

	@Test
	void removerDevolve204() throws Exception {
		mockMvc.perform(delete("/api/v1/dispositivos").param("token", "fcm-123")
				.header(HttpHeaders.AUTHORIZATION, BEARER))
				.andExpect(status().isNoContent());

		verify(dispositivoService).remover(ANA, "fcm-123");
	}

	@Test
	void semLoginNaoRemove() throws Exception {
		mockMvc.perform(delete("/api/v1/dispositivos").param("token", "fcm-123"))
				.andExpect(status().isUnauthorized());

		verify(dispositivoService, org.mockito.Mockito.never()).remover(any(), anyString());
	}

}

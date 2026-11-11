package com.bytestorm.waygo.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.bytestorm.waygo.application.dto.request.LoginRequest;
import com.bytestorm.waygo.application.dto.request.RegistroRequest;
import com.bytestorm.waygo.application.dto.response.LoginResponse;
import com.bytestorm.waygo.application.dto.response.RegistroResponse;
import com.bytestorm.waygo.application.service.AuthService;
import com.bytestorm.waygo.domain.exception.CredenciaisInvalidasException;
import com.bytestorm.waygo.domain.exception.EmailJaCadastradoException;
import com.bytestorm.waygo.domain.interfaces.TokenProvider;
import com.bytestorm.waygo.infrastructure.config.security.SecurityConfig;

/** Camada web com o SecurityConfig real; o AuthService é simulado. */
@WebMvcTest(AuthController.class)
@Import({ SecurityConfig.class, AuthControllerTest.Relogio.class })
class AuthControllerTest {

	@TestConfiguration
	static class Relogio {
		@Bean
		Clock clock() {
			return Clock.fixed(Instant.parse("2026-11-11T12:00:00Z"), ZoneOffset.UTC);
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	// Dependências do AuthFilter, que o @WebMvcTest carrega por ser um Filter
	@MockitoBean
	private TokenProvider tokenProvider;

	@MockitoBean
	private UserDetailsService userDetailsService;

	private static final String CORPO_VALIDO = """
			{"email": "ana@email.com", "senha": "senhaForte123"}
			""";

	@Test
	void registroDevolve201ComOUsuarioCriadoSemToken() throws Exception {
		UUID id = UUID.randomUUID();
		given(authService.registrar(any(RegistroRequest.class))).willReturn(
				new RegistroResponse(id, "ana@email.com", List.of("USUARIO"), LocalDateTime.of(2026, 11, 11, 9, 0)));

		mockMvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.email").value("ana@email.com"))
				.andExpect(jsonPath("$.roles[0]").value("USUARIO"))
				.andExpect(jsonPath("$.senha").doesNotExist())
				.andExpect(jsonPath("$.senhaHash").doesNotExist());
	}

	@Test
	void registroComEmailRepetidoDevolve409() throws Exception {
		given(authService.registrar(any(RegistroRequest.class))).willThrow(new EmailJaCadastradoException());

		mockMvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
	}

	@Test
	void registroComDadosInvalidosDevolve400SemChamarOService() throws Exception {
		mockMvc.perform(post("/api/v1/auth/registro").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "nao-eh-email", "senha": ""}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));

		verifyNoInteractions(authService);
	}

	@Test
	void loginDevolve200ComTokenEExpiracao() throws Exception {
		given(authService.login(any(LoginRequest.class))).willReturn(
				new LoginResponse("token.jwt.assinado", "Bearer", Instant.parse("2026-11-11T13:00:00Z"), 3600));

		mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("token.jwt.assinado"))
				.andExpect(jsonPath("$.tipo").value("Bearer"))
				.andExpect(jsonPath("$.expiraEm").value("2026-11-11T13:00:00Z"))
				.andExpect(jsonPath("$.expiraEmSegundos").value(3600));
	}

	@Test
	void loginComCredenciaisErradasDevolve401ComMensagem() throws Exception {
		given(authService.login(any(LoginRequest.class))).willThrow(new CredenciaisInvalidasException());

		mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("E-mail ou senha inválidos"));
	}

	@Test
	void loginComJsonMalformadoDevolve400() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{email:"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Corpo da requisição ausente ou com JSON inválido"));
	}

	@Test
	void rotasForaDeAuthExigemToken() throws Exception {
		mockMvc.perform(get("/api/v1/rede"))
				.andExpect(status().isUnauthorized());
	}

}

package com.bytestorm.waygo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.JWT;
import com.bytestorm.waygo.application.dto.request.LoginRequest;
import com.bytestorm.waygo.application.dto.request.RegistroRequest;
import com.bytestorm.waygo.application.dto.response.LoginResponse;
import com.bytestorm.waygo.application.dto.response.RegistroResponse;
import com.bytestorm.waygo.domain.entity.Usuario;
import com.bytestorm.waygo.domain.exception.CredenciaisInvalidasException;
import com.bytestorm.waygo.domain.exception.EmailJaCadastradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.UsuarioRepository;
import com.bytestorm.waygo.infrastructure.config.security.BCryptSenhaHasher;
import com.bytestorm.waygo.infrastructure.config.security.TokenService;
import com.bytestorm.waygo.infrastructure.config.security.UsuarioDetailsService;

/** Usa as peças reais (UsuarioDetailsService, BCrypt, AuthenticationManager e TokenService); só o banco é simulado. */
class AuthServiceTest {

	private static final long EXPIRACAO_MINUTOS = 60L;
	private static final Instant AGORA = Instant.parse("2026-11-11T12:00:00Z");

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final BCryptSenhaHasher senhaHasher = new BCryptSenhaHasher(passwordEncoder);
	private final RepositorioEmMemoria repositorio = new RepositorioEmMemoria();
	private final TokenService tokenService = criarTokenService();
	private final AuthService authService = criarAuthService(repositorio, Clock.system(ZoneOffset.UTC));

	/** Simula o adapter: guarda por id e busca por e-mail sem diferenciar maiúsculas. */
	private static class RepositorioEmMemoria implements UsuarioRepository {

		private final Map<UUID, Usuario> usuarios = new HashMap<>();
		private boolean violarIndiceUnicoNoProximoSalvar;

		@Override
		public Usuario salvar(Usuario usuario) {
			if (violarIndiceUnicoNoProximoSalvar) {
				throw new DataIntegrityViolationException("UK_USUARIO_EMAIL");
			}
			usuarios.put(usuario.getId(), usuario);
			return usuario;
		}

		@Override
		public Optional<Usuario> buscarPorId(UUID id) {
			return Optional.ofNullable(usuarios.get(id));
		}

		@Override
		public Optional<Usuario> buscarPorEmail(String email) {
			return usuarios.values().stream().filter(u -> u.possuiEmail(email)).findFirst();
		}

		@Override
		public boolean existePorEmail(String email) {
			return buscarPorEmail(email).isPresent();
		}
	}

	private static TokenService criarTokenService() {
		TokenService tokenService = new TokenService();
		ReflectionTestUtils.setField(tokenService, "SEGREDO", "segredo-de-teste-com-mais-de-32-caracteres");
		ReflectionTestUtils.setField(tokenService, "EMISSOR", "way-go");
		ReflectionTestUtils.setField(tokenService, "EXPIRACAO_MINUTOS", EXPIRACAO_MINUTOS);
		return tokenService;
	}

	private AuthService criarAuthService(UsuarioRepository repo, Clock clock) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(new UsuarioDetailsService(repo));
		provider.setPasswordEncoder(passwordEncoder);
		return new AuthService(repo, tokenService, new ProviderManager(provider), senhaHasher, clock);
	}

	private RegistroResponse registrarAna() {
		return authService.registrar(new RegistroRequest("ana@email.com", "senhaForte123"));
	}

	// --- registro ---

	@Test
	void registrarCriaUsuarioComRoleUsuarioESenhaEmHash() {
		AuthService comRelogioFixo = criarAuthService(repositorio, Clock.fixed(AGORA, ZoneOffset.UTC));

		RegistroResponse resposta = comRelogioFixo.registrar(new RegistroRequest(" Ana@Email.com ", "senhaForte123"));

		assertThat(resposta.id()).isNotNull();
		assertThat(resposta.email()).isEqualTo("ana@email.com");
		assertThat(resposta.roles()).containsExactly("USUARIO");
		assertThat(resposta.criadoEm()).isEqualTo(LocalDateTime.of(2026, 11, 11, 12, 0));

		Usuario salvo = repositorio.buscarPorId(resposta.id()).orElseThrow();
		assertThat(salvo.getSenhaHash()).startsWith("$2").isNotEqualTo("senhaForte123");
		assertThat(salvo.verificarSenha("senhaForte123", senhaHasher)).isTrue();
	}

	@Test
	void registrarRecusaEmailJaCadastradoSemDiferenciarMaiusculas() {
		registrarAna();

		assertThatThrownBy(() -> authService.registrar(new RegistroRequest("ANA@email.com", "outraSenha123")))
				.isInstanceOf(EmailJaCadastradoException.class)
				.hasMessage("E-mail já cadastrado");
	}

	@Test
	void registrarTraduzViolacaoDoIndiceUnicoEmEmailJaCadastrado() {
		repositorio.violarIndiceUnicoNoProximoSalvar = true;

		assertThatThrownBy(this::registrarAna).isInstanceOf(EmailJaCadastradoException.class);
	}

	@Test
	void registrarTraduzRegrasDoDominioEmRequisicaoInvalida() {
		assertThatThrownBy(() -> authService.registrar(new RegistroRequest("ana@email.com", "curta")))
				.isInstanceOf(RequisicaoInvalidaException.class)
				.hasMessageContaining("8");
		assertThatThrownBy(() -> authService.registrar(new RegistroRequest("ana@email", "senhaForte123")))
				.isInstanceOf(RequisicaoInvalidaException.class);
		assertThat(repositorio.usuarios).isEmpty();
	}

	// --- login ---

	@Test
	void usuarioRegistradoConsegueLogarERecebeTokenComExpiracao() {
		registrarAna();
		Instant antes = Instant.now().truncatedTo(ChronoUnit.SECONDS);

		LoginResponse resposta = authService.login(new LoginRequest("ana@email.com", "senhaForte123"));

		assertThat(resposta.tipo()).isEqualTo("Bearer");
		assertThat(tokenService.validarToken(resposta.token())).contains("ana@email.com");
		assertThat(resposta.expiraEm()).isEqualTo(JWT.decode(resposta.token()).getExpiresAtAsInstant());
		assertThat(resposta.expiraEm()).isAfterOrEqualTo(antes.plus(EXPIRACAO_MINUTOS, ChronoUnit.MINUTES));
		assertThat(resposta.expiraEmSegundos()).isBetween(EXPIRACAO_MINUTOS * 60 - 5, EXPIRACAO_MINUTOS * 60);
	}

	@Test
	void loginAceitaEmailComMaiusculasEEspacos() {
		registrarAna();

		LoginResponse resposta = authService.login(new LoginRequest("  ANA@Email.com ", "senhaForte123"));

		assertThat(tokenService.validarToken(resposta.token())).contains("ana@email.com");
	}

	@Test
	void senhaErradaEEmailInexistenteDaoOMesmoErro() {
		registrarAna();

		assertThatThrownBy(() -> authService.login(new LoginRequest("ana@email.com", "senhaErrada1")))
				.isInstanceOf(CredenciaisInvalidasException.class)
				.hasMessage("E-mail ou senha inválidos");
		assertThatThrownBy(() -> authService.login(new LoginRequest("nao.existe@email.com", "senhaForte123")))
				.isInstanceOf(CredenciaisInvalidasException.class)
				.hasMessage("E-mail ou senha inválidos");
	}

}

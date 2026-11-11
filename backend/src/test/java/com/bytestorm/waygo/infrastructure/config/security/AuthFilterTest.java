package com.bytestorm.waygo.infrastructure.config.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.bytestorm.waygo.domain.entity.Usuario;

class AuthFilterTest {

	private final Usuario usuario = Usuario.criar("ana@email.com", "senhaForte123",
			new BCryptSenhaHasher(new BCryptPasswordEncoder()), LocalDateTime.now());

	private final TokenService tokenService = criarTokenService();

	/** Só conhece a "ana"; qualquer outro e-mail se comporta como usuário removido. */
	private final UserDetailsService userDetailsService = email -> {
		if (usuario.possuiEmail(email)) {
			return new UsuarioAutenticado(usuario);
		}
		throw new UsernameNotFoundException("Usuário não encontrado");
	};

	private final AuthFilter filtro = new AuthFilter(tokenService, userDetailsService);

	private static TokenService criarTokenService() {
		TokenService tokenService = new TokenService();
		ReflectionTestUtils.setField(tokenService, "SEGREDO", "segredo-de-teste-com-mais-de-32-caracteres");
		ReflectionTestUtils.setField(tokenService, "EMISSOR", "way-go");
		ReflectionTestUtils.setField(tokenService, "EXPIRACAO_MINUTOS", 60L);
		return tokenService;
	}

	@AfterEach
	void limparContexto() {
		SecurityContextHolder.clearContext();
	}

	/** Executa o filtro e devolve a autenticação vista pelo próximo elemento da cadeia. */
	private Authentication filtrar(String authorization) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/rede");
		if (authorization != null) {
			request.addHeader("Authorization", authorization);
		}
		Authentication[] vista = new Authentication[1];
		MockFilterChain chain = new MockFilterChain(new jakarta.servlet.http.HttpServlet() {
			@Override
			protected void service(jakarta.servlet.http.HttpServletRequest req,
					jakarta.servlet.http.HttpServletResponse res) {
				vista[0] = SecurityContextHolder.getContext().getAuthentication();
			}
		});

		filtro.doFilter(request, new MockHttpServletResponse(), chain);

		assertThat(chain.getRequest()).as("a requisição sempre segue na cadeia").isNotNull();
		return vista[0];
	}

	@Test
	void autenticaComTokenValido() throws Exception {
		Authentication autenticacao = filtrar("Bearer " + tokenService.gerarToken(usuario));

		assertThat(autenticacao).isNotNull();
		assertThat(autenticacao.isAuthenticated()).isTrue();
		assertThat(autenticacao.getPrincipal()).isInstanceOfSatisfying(UsuarioAutenticado.class,
				principal -> assertThat(principal.getUsuario().getId()).isEqualTo(usuario.getId()));
		assertThat(autenticacao.getAuthorities()).extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_USUARIO");
	}

	@Test
	void aceitaPrefixoBearerEmMinusculas() throws Exception {
		assertThat(filtrar("bearer " + tokenService.gerarToken(usuario))).isNotNull();
	}

	@Test
	void naoAutenticaSemTokenOuComTokenInvalido() throws Exception {
		assertThat(filtrar(null)).isNull();
		assertThat(filtrar("Bearer ")).isNull();
		assertThat(filtrar("Basic YW5hOnNlbmhh")).isNull();
		assertThat(filtrar("Bearer token-invalido")).isNull();
	}

	@Test
	void naoAutenticaSeOUsuarioDoTokenNaoExisteMais() throws Exception {
		Usuario removido = Usuario.criar("removido@email.com", "senhaForte123",
				new BCryptSenhaHasher(new BCryptPasswordEncoder()), LocalDateTime.now());

		assertThat(filtrar("Bearer " + tokenService.gerarToken(removido))).isNull();
	}

}

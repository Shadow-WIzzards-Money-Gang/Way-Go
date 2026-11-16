package com.bytestorm.waygo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.application.dto.request.RedefinirSenhaRequest;
import com.bytestorm.waygo.domain.entity.TokenRecuperacaoSenha;
import com.bytestorm.waygo.domain.entity.Usuario;
import com.bytestorm.waygo.domain.enums.Role;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.EnviadorEmail;
import com.bytestorm.waygo.domain.interfaces.SenhaHasher;
import com.bytestorm.waygo.domain.interfaces.TokenRecuperacaoSenhaRepository;
import com.bytestorm.waygo.domain.interfaces.UsuarioRepository;
import com.bytestorm.waygo.domain.valueobject.Pagina;

class RecuperacaoSenhaServiceTest {

	private static final Instant INICIO = Instant.parse("2026-11-16T10:00:00Z");
	private static final String EMAIL = "ana@email.com";

	/** Hasher de teste: o "hash" é o texto com prefixo, o bastante para conferir senha e código. */
	private static final SenhaHasher HASHER = new SenhaHasher() {
		@Override
		public String gerarHash(String senha) {
			return "hash:" + senha;
		}

		@Override
		public boolean corresponde(String senha, String hash) {
			return hash.equals(gerarHash(senha));
		}
	};

	private final Usuarios usuarios = new Usuarios();
	private final Tokens tokens = new Tokens();
	private final Emails emails = new Emails();
	private final RelogioAjustavel relogio = new RelogioAjustavel(INICIO);
	private final List<String> codigosGerados = new ArrayList<>(List.of("111111", "222222", "333333"));
	private final RecuperacaoSenhaService service = new RecuperacaoSenhaService(usuarios, tokens, HASHER,
			() -> codigosGerados.removeFirst(), emails, relogio);

	private final Usuario ana = usuarios.salvar(Usuario.reconstruir(UUID.randomUUID(), EMAIL, "hash:senhaAntiga1",
			Set.of(Role.USUARIO), LocalDateTime.of(2026, 1, 1, 10, 0)));

	// --- solicitar ---

	@Test
	void solicitarGeraCodigoGuardaSoOHashEEnviaPorEmail() {
		service.solicitar("Ana@Email.com");

		assertThat(emails.codigos).containsExactly(EMAIL + ":111111");
		TokenRecuperacaoSenha token = tokens.todos.getFirst();
		assertThat(token.getIdUsuario()).isEqualTo(ana.getId());
		assertThat(token.getCodigoHash()).isEqualTo("hash:111111");
		assertThat(token.getExpiraEm()).isEqualTo(agora().plusMinutes(15));
	}

	@Test
	void solicitarComEmailSemContaNaoFalhaENaoEnvia() {
		service.solicitar("ninguem@email.com");

		assertThat(emails.codigos).isEmpty();
		assertThat(tokens.todos).isEmpty();
	}

	@Test
	void solicitarDeNovoEmMenosDeUmMinutoNaoReenvia() {
		service.solicitar(EMAIL);
		relogio.avancar(Duration.ofSeconds(30));
		service.solicitar(EMAIL);

		assertThat(emails.codigos).hasSize(1);
		assertThat(tokens.todos).hasSize(1);
	}

	@Test
	void novoPedidoInvalidaOCodigoAnterior() {
		service.solicitar(EMAIL);
		relogio.avancar(Duration.ofMinutes(2));
		service.solicitar(EMAIL);

		assertThat(emails.codigos).containsExactly(EMAIL + ":111111", EMAIL + ":222222");
		assertThatThrownBy(() -> service.redefinir(pedido("111111")))
				.isInstanceOf(RequisicaoInvalidaException.class);
		service.redefinir(pedido("222222"));
		assertThat(senhaAtualDaAna()).isEqualTo("hash:novaSenha123");
	}

	// --- redefinir ---

	@Test
	void codigoCertoTrocaASenhaEAvisaPorEmail() {
		service.solicitar(EMAIL);

		service.redefinir(pedido("111111"));

		assertThat(senhaAtualDaAna()).isEqualTo("hash:novaSenha123");
		assertThat(tokens.todos.getFirst().getUsadoEm()).isEqualTo(agora());
		assertThat(emails.senhasRedefinidas).containsExactly(EMAIL);
	}

	@Test
	void codigoJaUsadoNaoServeDeNovo() {
		service.solicitar(EMAIL);
		service.redefinir(pedido("111111"));

		assertThatThrownBy(() -> service.redefinir(pedido("111111")))
				.isInstanceOf(RequisicaoInvalidaException.class)
				.hasMessage(RecuperacaoSenhaService.MENSAGEM_CODIGO_INVALIDO);
	}

	@Test
	void codigoErradoContaTentativaEBloqueiaDepoisDeCinco() {
		service.solicitar(EMAIL);

		for (int i = 1; i <= TokenRecuperacaoSenha.MAX_TENTATIVAS; i++) {
			assertThatThrownBy(() -> service.redefinir(pedido("999999")))
					.isInstanceOf(RequisicaoInvalidaException.class)
					.hasMessage(RecuperacaoSenhaService.MENSAGEM_CODIGO_INVALIDO);
			assertThat(tokens.todos.getFirst().getTentativas()).isEqualTo(i);
		}

		// Esgotadas as tentativas, nem o código certo vale mais.
		assertThatThrownBy(() -> service.redefinir(pedido("111111")))
				.isInstanceOf(RequisicaoInvalidaException.class);
		assertThat(senhaAtualDaAna()).isEqualTo("hash:senhaAntiga1");
		assertThat(emails.senhasRedefinidas).isEmpty();
	}

	@Test
	void codigoExpiradoEhRecusado() {
		service.solicitar(EMAIL);
		relogio.avancar(Duration.ofMinutes(15));

		assertThatThrownBy(() -> service.redefinir(pedido("111111")))
				.isInstanceOf(RequisicaoInvalidaException.class);
		assertThat(senhaAtualDaAna()).isEqualTo("hash:senhaAntiga1");
	}

	@Test
	void emailSemContaOuSemCodigoDevolveAMesmaMensagem() {
		assertThatThrownBy(() -> service.redefinir(new RedefinirSenhaRequest("ninguem@email.com", "111111",
				"novaSenha123")))
				.isInstanceOf(RequisicaoInvalidaException.class)
				.hasMessage(RecuperacaoSenhaService.MENSAGEM_CODIGO_INVALIDO);
		assertThatThrownBy(() -> service.redefinir(pedido("111111")))
				.isInstanceOf(RequisicaoInvalidaException.class)
				.hasMessage(RecuperacaoSenhaService.MENSAGEM_CODIGO_INVALIDO);
	}

	@Test
	void senhaForaDasRegrasNaoConsomeOCodigo() {
		service.solicitar(EMAIL);

		assertThatThrownBy(() -> service.redefinir(new RedefinirSenhaRequest(EMAIL, "111111", "        ")))
				.isInstanceOf(RequisicaoInvalidaException.class);

		assertThat(tokens.todos.getFirst().getUsadoEm()).isNull();
		service.redefinir(pedido("111111"));
		assertThat(senhaAtualDaAna()).isEqualTo("hash:novaSenha123");
	}

	// --- apoio ---

	private static RedefinirSenhaRequest pedido(String codigo) {
		return new RedefinirSenhaRequest(EMAIL, codigo, "novaSenha123");
	}

	private String senhaAtualDaAna() {
		return usuarios.buscarPorId(ana.getId()).orElseThrow().getSenhaHash();
	}

	private LocalDateTime agora() {
		return LocalDateTime.now(relogio);
	}

	private static class RelogioAjustavel extends Clock {

		private Instant instante;

		RelogioAjustavel(Instant instante) {
			this.instante = instante;
		}

		void avancar(Duration duracao) {
			instante = instante.plus(duracao);
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instant instant() {
			return instante;
		}
	}

	private static class Usuarios implements UsuarioRepository {

		final List<Usuario> todos = new ArrayList<>();

		@Override
		public Usuario salvar(Usuario usuario) {
			todos.removeIf(u -> u.getId().equals(usuario.getId()));
			todos.add(usuario);
			return usuario;
		}

		@Override
		public Optional<Usuario> buscarPorId(UUID id) {
			return todos.stream().filter(u -> u.getId().equals(id)).findFirst();
		}

		@Override
		public Optional<Usuario> buscarPorEmail(String email) {
			return todos.stream().filter(u -> u.possuiEmail(email)).findFirst();
		}

		@Override
		public boolean existePorEmail(String email) {
			return buscarPorEmail(email).isPresent();
		}

		@Override
		public Pagina<Usuario> listarPaginado(int pagina, int tamanho) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void excluir(UUID id) {
			throw new UnsupportedOperationException();
		}
	}

	/** Simula o adapter: gera ids e guarda cópias, como o banco faria. */
	private static class Tokens implements TokenRecuperacaoSenhaRepository {

		final List<TokenRecuperacaoSenha> todos = new ArrayList<>();
		private long proximoId = 1;

		@Override
		public TokenRecuperacaoSenha salvar(TokenRecuperacaoSenha token) {
			Long id = token.getId() != null ? token.getId() : proximoId++;
			TokenRecuperacaoSenha salvo = TokenRecuperacaoSenha.reconstruir(id, token.getIdUsuario(),
					token.getCodigoHash(), token.getExpiraEm(), token.getTentativas(), token.getUsadoEm(),
					token.getCriadoEm());
			todos.removeIf(t -> t.getId().equals(id));
			todos.add(salvo);
			return salvo;
		}

		@Override
		public Optional<TokenRecuperacaoSenha> buscarMaisRecenteDoUsuario(UUID idUsuario) {
			return todos.stream()
					.filter(t -> t.getIdUsuario().equals(idUsuario))
					.max(Comparator.comparing(TokenRecuperacaoSenha::getCriadoEm)
							.thenComparing(TokenRecuperacaoSenha::getId))
					.map(t -> TokenRecuperacaoSenha.reconstruir(t.getId(), t.getIdUsuario(), t.getCodigoHash(),
							t.getExpiraEm(), t.getTentativas(), t.getUsadoEm(), t.getCriadoEm()));
		}

		@Override
		public void invalidarAtivosDoUsuario(UUID idUsuario, LocalDateTime agora) {
			for (TokenRecuperacaoSenha token : List.copyOf(todos)) {
				if (token.getIdUsuario().equals(idUsuario) && token.getUsadoEm() == null) {
					token.marcarUsado(agora);
				}
			}
		}
	}

	private static class Emails implements EnviadorEmail {

		final List<String> codigos = new ArrayList<>();
		final List<String> senhasRedefinidas = new ArrayList<>();

		@Override
		public void enviarBoasVindas(String destinatario) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void enviarContaExcluida(String destinatario) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void enviarCodigoRecuperacao(String destinatario, String codigo) {
			codigos.add(destinatario + ":" + codigo);
		}

		@Override
		public void enviarSenhaRedefinida(String destinatario) {
			senhasRedefinidas.add(destinatario);
		}
	}

}

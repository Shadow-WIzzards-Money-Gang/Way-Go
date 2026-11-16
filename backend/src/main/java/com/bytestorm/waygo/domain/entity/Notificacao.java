package com.bytestorm.waygo.domain.entity;

import static com.bytestorm.waygo.domain.util.Validacoes.exigir;
import static com.bytestorm.waygo.domain.util.Validacoes.exigirTexto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.bytestorm.waygo.domain.enums.StatusNotificacao;
import com.bytestorm.waygo.domain.enums.TipoNotificacao;

import lombok.Getter;

@Getter
public class Notificacao {

	private static final int TAMANHO_CHAVE = 150;
	private static final int TAMANHO_TITULO = 150;
	private static final int TAMANHO_CORPO = 1000;
	private static final int TAMANHO_ERRO = 500;

	static final List<Duration> ESPERAS = List.of(Duration.ofSeconds(30), Duration.ofMinutes(1),
			Duration.ofMinutes(2), Duration.ofMinutes(5), Duration.ofMinutes(10), Duration.ofMinutes(15));

	private final Long id;
	private final UUID idUsuario;
	private final TipoNotificacao tipo;
	private final String chave;
	private final String titulo;
	private final String corpo;
	private final Map<String, String> dados;
	private final LocalDateTime expiraEm;
	private final LocalDateTime criadoEm;
	private StatusNotificacao status;
	private int tentativas;
	private LocalDateTime proximaTentativaEm;
	private String ultimoErro;
	private LocalDateTime enviadaEm;

	private Notificacao(Long id, UUID idUsuario, TipoNotificacao tipo, String chave, String titulo, String corpo,
			Map<String, String> dados, StatusNotificacao status, int tentativas, LocalDateTime proximaTentativaEm,
			LocalDateTime expiraEm, String ultimoErro, LocalDateTime criadoEm, LocalDateTime enviadaEm) {
		this.id = id;
		this.idUsuario = exigir(idUsuario, "idUsuario");
		this.tipo = exigir(tipo, "tipo");
		this.chave = exigirTexto(chave, "chave", TAMANHO_CHAVE);
		this.titulo = cortar(exigirTexto(titulo, "titulo", Integer.MAX_VALUE), TAMANHO_TITULO);
		this.corpo = cortar(exigirTexto(corpo, "corpo", Integer.MAX_VALUE), TAMANHO_CORPO);
		this.dados = dados == null ? Map.of() : Map.copyOf(dados);
		this.status = exigir(status, "status");
		if (tentativas < 0) {
			throw new IllegalArgumentException("tentativas não pode ser negativo");
		}
		this.tentativas = tentativas;
		this.proximaTentativaEm = exigir(proximaTentativaEm, "proximaTentativaEm");
		this.expiraEm = exigir(expiraEm, "expiraEm");
		this.ultimoErro = ultimoErro;
		this.criadoEm = exigir(criadoEm, "criadoEm");
		this.enviadaEm = enviadaEm;
	}

	public static Notificacao criar(UUID idUsuario, TipoNotificacao tipo, String chave, String titulo, String corpo,
			Map<String, String> dados, LocalDateTime expiraEm, LocalDateTime agora) {
		return new Notificacao(null, idUsuario, tipo, chave, titulo, corpo, dados, StatusNotificacao.PENDENTE, 0,
				agora, expiraEm, null, agora, null);
	}

	public static Notificacao reconstruir(Long id, UUID idUsuario, TipoNotificacao tipo, String chave, String titulo,
			String corpo, Map<String, String> dados, StatusNotificacao status, int tentativas,
			LocalDateTime proximaTentativaEm, LocalDateTime expiraEm, String ultimoErro, LocalDateTime criadoEm,
			LocalDateTime enviadaEm) {
		return new Notificacao(id, idUsuario, tipo, chave, titulo, corpo, dados, status, tentativas,
				proximaTentativaEm, expiraEm, ultimoErro, criadoEm, enviadaEm);
	}

	public boolean estaPendente() {
		return status == StatusNotificacao.PENDENTE;
	}

	public boolean venceu(LocalDateTime agora) {
		return !agora.isBefore(expiraEm);
	}

	/** Marca como expirada se o prazo passou; devolve true nesse caso. */
	public boolean expirarSeVencida(LocalDateTime agora) {
		if (estaPendente() && venceu(agora)) {
			status = StatusNotificacao.EXPIRADA;
			return true;
		}
		return false;
	}

	public void marcarEnviada(LocalDateTime agora) {
		exigirPendente();
		tentativas++;
		status = StatusNotificacao.ENVIADA;
		enviadaEm = exigir(agora, "agora");
		ultimoErro = null;
	}

	/** Falhou: agenda a próxima tentativa (30 s, 1, 2, 5, 10 e depois a cada 15 min). */
	public void registrarFalha(String erro, LocalDateTime agora) {
		exigirPendente();
		tentativas++;
		ultimoErro = erro == null ? null : cortar(erro, TAMANHO_ERRO);
		Duration espera = ESPERAS.get(Math.min(tentativas, ESPERAS.size()) - 1);
		proximaTentativaEm = exigir(agora, "agora").plus(espera);
	}

	private void exigirPendente() {
		if (!estaPendente()) {
			throw new IllegalStateException("a notificação não está pendente");
		}
	}

	private static String cortar(String texto, int tamanho) {
		return texto.length() <= tamanho ? texto : texto.substring(0, tamanho - 1) + "…";
	}

}

package com.bytestorm.waygo.domain.entity;

import static com.bytestorm.waygo.domain.util.Validacoes.exigir;
import static com.bytestorm.waygo.domain.util.Validacoes.exigirTexto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bytestorm.waygo.domain.interfaces.SenhaHasher;

import lombok.Getter;

@Getter
public class TokenRecuperacaoSenha {

	public static final Duration VALIDADE = Duration.ofMinutes(15);
	public static final int MAX_TENTATIVAS = 5;
	private static final int TAMANHO_HASH = 255;

	private final Long id;
	private final UUID idUsuario;
	private final String codigoHash;
	private final LocalDateTime expiraEm;
	private final LocalDateTime criadoEm;
	private int tentativas;
	private LocalDateTime usadoEm;

	private TokenRecuperacaoSenha(Long id, UUID idUsuario, String codigoHash, LocalDateTime expiraEm, int tentativas,
			LocalDateTime usadoEm, LocalDateTime criadoEm) {
		this.id = id;
		this.idUsuario = exigir(idUsuario, "idUsuario");
		this.codigoHash = exigirTexto(codigoHash, "codigoHash", TAMANHO_HASH);
		this.expiraEm = exigir(expiraEm, "expiraEm");
		if (tentativas < 0) {
			throw new IllegalArgumentException("tentativas não pode ser negativo");
		}
		this.tentativas = tentativas;
		this.usadoEm = usadoEm;
		this.criadoEm = exigir(criadoEm, "criadoEm");
	}

	public static TokenRecuperacaoSenha criar(UUID idUsuario, String codigo, SenhaHasher hasher, LocalDateTime agora) {
		exigir(agora, "agora");
		String hash = exigir(hasher, "hasher").gerarHash(exigirTexto(codigo, "codigo", TAMANHO_HASH));
		return new TokenRecuperacaoSenha(null, idUsuario, hash, agora.plus(VALIDADE), 0, null, agora);
	}

	public static TokenRecuperacaoSenha reconstruir(Long id, UUID idUsuario, String codigoHash, LocalDateTime expiraEm,
			int tentativas, LocalDateTime usadoEm, LocalDateTime criadoEm) {
		return new TokenRecuperacaoSenha(id, idUsuario, codigoHash, expiraEm, tentativas, usadoEm, criadoEm);
	}

	/** Ainda pode ser usado: não foi usado, não expirou e não esgotou as tentativas. */
	public boolean estaValido(LocalDateTime agora) {
		return usadoEm == null && agora.isBefore(expiraEm) && tentativas < MAX_TENTATIVAS;
	}

	public boolean confere(String codigo, SenhaHasher hasher) {
		return codigo != null && exigir(hasher, "hasher").corresponde(codigo, codigoHash);
	}

	public void registrarTentativaFalha() {
		tentativas++;
	}

	public void marcarUsado(LocalDateTime agora) {
		if (usadoEm != null) {
			throw new IllegalStateException("código de recuperação já utilizado");
		}
		this.usadoEm = exigir(agora, "agora");
	}

	public boolean foiCriadoHaMenosDe(Duration intervalo, LocalDateTime agora) {
		return criadoEm.plus(intervalo).isAfter(agora);
	}

}

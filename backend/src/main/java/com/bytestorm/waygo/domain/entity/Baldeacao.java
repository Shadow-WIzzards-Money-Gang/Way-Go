package com.bytestorm.waygo.domain.entity;

import static com.bytestorm.waygo.domain.util.Validacoes.exigir;
import static com.bytestorm.waygo.domain.util.Validacoes.exigirDiferentes;
import static com.bytestorm.waygo.domain.util.Validacoes.positivoOuNulo;

import lombok.Getter;

@Getter
public class Baldeacao {

	private final Long id;
	private final Long linhaEstacaoOrigemId;
	private final Long linhaEstacaoDestinoId;
	private Integer tempoBaldeacaoSegundos;
	private boolean ativa;

	private Baldeacao(Long id, Long linhaEstacaoOrigemId, Long linhaEstacaoDestinoId,
			Integer tempoBaldeacaoSegundos, boolean ativa) {
		this.id = id;
		this.linhaEstacaoOrigemId = exigir(linhaEstacaoOrigemId, "linhaEstacaoOrigemId");
		this.linhaEstacaoDestinoId = exigir(linhaEstacaoDestinoId, "linhaEstacaoDestinoId");
		exigirDiferentes(linhaEstacaoOrigemId, linhaEstacaoDestinoId);
		this.tempoBaldeacaoSegundos = validarTempo(tempoBaldeacaoSegundos);
		this.ativa = ativa;
	}

	public static Baldeacao criar(Long linhaEstacaoOrigemId, Long linhaEstacaoDestinoId,
			Integer tempoBaldeacaoSegundos) {
		return reconstruir(null, linhaEstacaoOrigemId, linhaEstacaoDestinoId, tempoBaldeacaoSegundos, true);
	}

	public static Baldeacao reconstruir(Long id, Long linhaEstacaoOrigemId, Long linhaEstacaoDestinoId,
			Integer tempoBaldeacaoSegundos, boolean ativa) {
		return new Baldeacao(id, linhaEstacaoOrigemId, linhaEstacaoDestinoId, tempoBaldeacaoSegundos, ativa);
	}

	public void ativar() {
		this.ativa = true;
	}

	public void desativar() {
		this.ativa = false;
	}

	public void atualizarTempo(Integer segundos) {
		this.tempoBaldeacaoSegundos = validarTempo(segundos);
	}

	public boolean podeSerUtilizada() {
		return ativa && tempoBaldeacaoSegundos != null;
	}

	private static Integer validarTempo(Integer segundos) {
		return positivoOuNulo(segundos, "tempoBaldeacaoSegundos");
	}

}

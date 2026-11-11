package com.bytestorm.waygo.domain.entity;

import static com.bytestorm.waygo.domain.util.Validacoes.exigir;

import lombok.Getter;

@Getter
public class LinhaEstacao {

	private final Long id;
	private final Long linhaId;
	private final Long estacaoId;
	private int sequencia;
	private boolean ativa;

	private LinhaEstacao(Long id, Long linhaId, Long estacaoId, int sequencia, boolean ativa) {
		this.id = id;
		this.linhaId = exigir(linhaId, "linhaId");
		this.estacaoId = exigir(estacaoId, "estacaoId");
		this.sequencia = validarSequencia(sequencia);
		this.ativa = ativa;
	}

	public static LinhaEstacao criar(Long linhaId, Long estacaoId, int sequencia) {
		return reconstruir(null, linhaId, estacaoId, sequencia, true);
	}

	public static LinhaEstacao reconstruir(Long id, Long linhaId, Long estacaoId, int sequencia, boolean ativa) {
		return new LinhaEstacao(id, linhaId, estacaoId, sequencia, ativa);
	}

	public void ativar() {
		this.ativa = true;
	}

	public void desativar() {
		this.ativa = false;
	}

	public void reposicionar(int novaSequencia) {
		this.sequencia = validarSequencia(novaSequencia);
	}

	public boolean pertenceALinha(Long outraLinhaId) {
		return linhaId.equals(outraLinhaId);
	}

	private static int validarSequencia(int sequencia) {
		if (sequencia <= 0) {
			throw new IllegalArgumentException("sequencia deve ser maior que zero");
		}
		return sequencia;
	}

}

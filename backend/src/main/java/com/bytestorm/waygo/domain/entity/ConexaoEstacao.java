package com.bytestorm.waygo.domain.entity;

import static com.bytestorm.waygo.domain.util.Validacoes.exigir;
import static com.bytestorm.waygo.domain.util.Validacoes.exigirDiferentes;
import static com.bytestorm.waygo.domain.util.Validacoes.positivoOuNulo;

import lombok.Getter;

@Getter
public class ConexaoEstacao {

	private final Long id;
	private final Long linhaEstacaoOrigemId;
	private final Long linhaEstacaoDestinoId;
	private Integer tempoViagemSegundos;
	private Integer distanciaMetros;
	private boolean ativa;

	private ConexaoEstacao(Long id, Long linhaEstacaoOrigemId, Long linhaEstacaoDestinoId,
			Integer tempoViagemSegundos, Integer distanciaMetros, boolean ativa) {
		this.id = id;
		this.linhaEstacaoOrigemId = exigir(linhaEstacaoOrigemId, "linhaEstacaoOrigemId");
		this.linhaEstacaoDestinoId = exigir(linhaEstacaoDestinoId, "linhaEstacaoDestinoId");
		exigirDiferentes(linhaEstacaoOrigemId, linhaEstacaoDestinoId);
		this.tempoViagemSegundos = validarTempo(tempoViagemSegundos);
		this.distanciaMetros = validarDistancia(distanciaMetros);
		this.ativa = ativa;
	}

	public static ConexaoEstacao criar(Long linhaEstacaoOrigemId, Long linhaEstacaoDestinoId,
			Integer tempoViagemSegundos, Integer distanciaMetros) {
		return reconstruir(null, linhaEstacaoOrigemId, linhaEstacaoDestinoId, tempoViagemSegundos, distanciaMetros,
				true);
	}

	public static ConexaoEstacao reconstruir(Long id, Long linhaEstacaoOrigemId, Long linhaEstacaoDestinoId,
			Integer tempoViagemSegundos, Integer distanciaMetros, boolean ativa) {
		return new ConexaoEstacao(id, linhaEstacaoOrigemId, linhaEstacaoDestinoId, tempoViagemSegundos,
				distanciaMetros, ativa);
	}

	public void ativar() {
		this.ativa = true;
	}

	public void desativar() {
		this.ativa = false;
	}

	public void atualizarTempoViagem(Integer segundos) {
		this.tempoViagemSegundos = validarTempo(segundos);
	}

	public void atualizarDistancia(Integer metros) {
		this.distanciaMetros = validarDistancia(metros);
	}

	public boolean podeSerPercorrida() {
		return ativa && tempoViagemSegundos != null;
	}

	private static Integer validarTempo(Integer segundos) {
		return positivoOuNulo(segundos, "tempoViagemSegundos");
	}

	private static Integer validarDistancia(Integer metros) {
		return positivoOuNulo(metros, "distanciaMetros");
	}

}

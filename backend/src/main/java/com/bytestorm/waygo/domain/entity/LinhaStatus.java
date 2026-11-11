package com.bytestorm.waygo.domain.entity;

import static com.bytestorm.waygo.domain.util.Validacoes.exigir;
import static com.bytestorm.waygo.domain.util.Validacoes.textoOpcional;

import java.time.LocalDateTime;

import com.bytestorm.waygo.domain.enums.OrigemStatus;
import com.bytestorm.waygo.domain.enums.StatusOperacional;

import lombok.Getter;

@Getter
public class LinhaStatus {

	private static final int TAMANHO_DESCRICAO = 255;

	private final Long id;
	private final Long linhaId;
	private final StatusOperacional status;
	private final String descricao;
	private final OrigemStatus origem;
	private final LocalDateTime atualizadoEm;

	private LinhaStatus(Long id, Long linhaId, StatusOperacional status, String descricao, OrigemStatus origem,
			LocalDateTime atualizadoEm) {
		this.id = id;
		this.linhaId = exigir(linhaId, "linhaId");
		this.status = exigir(status, "status");
		this.descricao = textoOpcional(descricao, "descricao", TAMANHO_DESCRICAO);
		this.origem = exigir(origem, "origem");
		this.atualizadoEm = exigir(atualizadoEm, "atualizadoEm");
	}

	public static LinhaStatus criar(Long linhaId, StatusOperacional status, String descricao, OrigemStatus origem,
			LocalDateTime atualizadoEm) {
		return reconstruir(null, linhaId, status, descricao, origem, atualizadoEm);
	}

	public static LinhaStatus reconstruir(Long id, Long linhaId, StatusOperacional status, String descricao,
			OrigemStatus origem, LocalDateTime atualizadoEm) {
		return new LinhaStatus(id, linhaId, status, descricao, origem, atualizadoEm);
	}

	public static LinhaStatus registrarPelaApi(Long linhaId, StatusOperacional status, String descricao,
			LocalDateTime momento) {
		return criar(linhaId, status, descricao, OrigemStatus.API, momento);
	}

	public static LinhaStatus registrarPeloAdministrador(Long linhaId, StatusOperacional status, String descricao,
			LocalDateTime momento) {
		return criar(linhaId, status, descricao, OrigemStatus.ADMINISTRADOR, momento);
	}

	public boolean bloqueiaCirculacao() {
		return status.bloqueiaCirculacao();
	}

	public boolean afetaTempo() {
		return status.afetaTempo();
	}

	public boolean mudouEmRelacaoA(StatusOperacional anterior) {
		return status != anterior;
	}

}

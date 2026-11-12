package com.bytestorm.waygo.domain.entity;

import static com.bytestorm.waygo.domain.util.Validacoes.exigir;
import static com.bytestorm.waygo.domain.util.Validacoes.exigirDiferentes;
import static com.bytestorm.waygo.domain.util.Validacoes.exigirTexto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.bytestorm.waygo.domain.enums.CriterioRota;

import lombok.Getter;

@Getter
public class RotaSalva {

	private static final int TAMANHO_NOME = 100;

	private final Long id;
	private final UUID idUsuario;
	private final LocalDateTime criadoEm;
	private Long idEstacaoOrigem;
	private Long idEstacaoDestino;
	private String nome;
	private CriterioRota criterioRota;

	private RotaSalva(Long id, Long idEstacaoOrigem, Long idEstacaoDestino, String nome, CriterioRota criterioRota,
			UUID idUsuario, LocalDateTime criadoEm) {
		this.id = id;
		this.idEstacaoOrigem = exigir(idEstacaoOrigem, "idEstacaoOrigem");
		this.idEstacaoDestino = exigir(idEstacaoDestino, "idEstacaoDestino");
		exigirDiferentes(idEstacaoOrigem, idEstacaoDestino);
		this.nome = exigirTexto(nome, "nome", TAMANHO_NOME);
		this.criterioRota = exigir(criterioRota, "criterioRota");
		this.idUsuario = exigir(idUsuario, "idUsuario");
		this.criadoEm = exigir(criadoEm, "criadoEm");
	}

	public static RotaSalva criar(Long idEstacaoOrigem, Long idEstacaoDestino, String nome, UUID idUsuario,
			LocalDateTime criadoEm) {
		return criar(idEstacaoOrigem, idEstacaoDestino, nome, CriterioRota.EQUILIBRADO, idUsuario, criadoEm);
	}

	public static RotaSalva criar(Long idEstacaoOrigem, Long idEstacaoDestino, String nome, CriterioRota criterioRota,
			UUID idUsuario, LocalDateTime criadoEm) {
		return reconstruir(null, idEstacaoOrigem, idEstacaoDestino, nome, criterioRota, idUsuario, criadoEm);
	}

	public static RotaSalva reconstruir(Long id, Long idEstacaoOrigem, Long idEstacaoDestino, String nome,
			CriterioRota criterioRota, UUID idUsuario, LocalDateTime criadoEm) {
		return new RotaSalva(id, idEstacaoOrigem, idEstacaoDestino, nome, criterioRota, idUsuario, criadoEm);
	}

	public void atualizarNome(String nome) {
		this.nome = exigirTexto(nome, "nome", TAMANHO_NOME);
	}

	public void atualizarEstacaoOrigem(Long idEstacaoOrigem) {
		exigir(idEstacaoOrigem, "idEstacaoOrigem");
		exigirDiferentes(idEstacaoOrigem, this.idEstacaoDestino);
		this.idEstacaoOrigem = idEstacaoOrigem;
	}

	public void atualizarEstacaoDestino(Long idEstacaoDestino) {
		exigir(idEstacaoDestino, "idEstacaoDestino");
		exigirDiferentes(this.idEstacaoOrigem, idEstacaoDestino);
		this.idEstacaoDestino = idEstacaoDestino;
	}

	public void atualizarCriterioRota(CriterioRota criterioRota) {
		this.criterioRota = exigir(criterioRota, "criterioRota");
	}

}

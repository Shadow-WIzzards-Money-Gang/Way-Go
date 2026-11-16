package com.bytestorm.waygo.domain.entity;

import static com.bytestorm.waygo.domain.util.Validacoes.exigir;
import static com.bytestorm.waygo.domain.util.Validacoes.exigirTexto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.bytestorm.waygo.domain.enums.Plataforma;

import lombok.Getter;

/** Aparelho que recebe push pelo FCM. O token é único: se outra conta entrar no aparelho, ele muda de dono. */
@Getter
public class DispositivoUsuario {

	private static final int TAMANHO_TOKEN = 512;

	private final Long id;
	private final String tokenFcm;
	private final LocalDateTime criadoEm;
	private UUID idUsuario;
	private Plataforma plataforma;
	private boolean alertasStatus;
	private boolean ativo;
	private LocalDateTime atualizadoEm;

	private DispositivoUsuario(Long id, UUID idUsuario, String tokenFcm, Plataforma plataforma, boolean alertasStatus,
			boolean ativo, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
		this.id = id;
		this.idUsuario = exigir(idUsuario, "idUsuario");
		this.tokenFcm = exigirTexto(tokenFcm, "tokenFcm", TAMANHO_TOKEN);
		this.plataforma = exigir(plataforma, "plataforma");
		this.alertasStatus = alertasStatus;
		this.ativo = ativo;
		this.criadoEm = exigir(criadoEm, "criadoEm");
		this.atualizadoEm = exigir(atualizadoEm, "atualizadoEm");
	}

	public static DispositivoUsuario criar(UUID idUsuario, String tokenFcm, Plataforma plataforma,
			boolean alertasStatus, LocalDateTime agora) {
		return new DispositivoUsuario(null, idUsuario, tokenFcm, plataforma, alertasStatus, true, agora, agora);
	}

	public static DispositivoUsuario reconstruir(Long id, UUID idUsuario, String tokenFcm, Plataforma plataforma,
			boolean alertasStatus, boolean ativo, LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
		return new DispositivoUsuario(id, idUsuario, tokenFcm, plataforma, alertasStatus, ativo, criadoEm,
				atualizadoEm);
	}

	public void registrarNovamente(UUID idUsuario, Plataforma plataforma, boolean alertasStatus, LocalDateTime agora) {
		this.idUsuario = exigir(idUsuario, "idUsuario");
		this.plataforma = exigir(plataforma, "plataforma");
		this.alertasStatus = alertasStatus;
		this.ativo = true;
		this.atualizadoEm = exigir(agora, "agora");
	}

	public void desativar(LocalDateTime agora) {
		this.ativo = false;
		this.atualizadoEm = exigir(agora, "agora");
	}

	public boolean pertenceA(UUID outroUsuario) {
		return idUsuario.equals(outroUsuario);
	}

}

package com.bytestorm.waygo.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.bytestorm.waygo.domain.enums.StatusNotificacao;
import com.bytestorm.waygo.domain.enums.TipoNotificacao;
import com.bytestorm.waygo.infrastructure.persistence.converter.DadosNotificacaoConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "NOTIFICACAO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificacaoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@JdbcTypeCode(SqlTypes.VARCHAR)
	@Column(name = "ID_USUARIO", length = 36, nullable = false, updatable = false)
	private UUID idUsuario;

	@Enumerated(EnumType.STRING)
	@Column(name = "TIPO", nullable = false, length = 30, updatable = false)
	private TipoNotificacao tipo;

	@Column(name = "CHAVE", nullable = false, length = 150, updatable = false)
	private String chave;

	@Column(name = "TITULO", nullable = false, length = 150, updatable = false)
	private String titulo;

	@Column(name = "CORPO", nullable = false, length = 1000, updatable = false)
	private String corpo;

	@Convert(converter = DadosNotificacaoConverter.class)
	@Column(name = "DADOS", length = 2000, updatable = false)
	private Map<String, String> dados;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false, length = 15)
	private StatusNotificacao status;

	@Column(name = "TENTATIVAS", nullable = false)
	private int tentativas;

	@Column(name = "PROXIMA_TENTATIVA_EM", nullable = false)
	private LocalDateTime proximaTentativaEm;

	@Column(name = "EXPIRA_EM", nullable = false, updatable = false)
	private LocalDateTime expiraEm;

	@Column(name = "ULTIMO_ERRO", length = 500)
	private String ultimoErro;

	@Column(name = "CRIADO_EM", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "ENVIADA_EM")
	private LocalDateTime enviadaEm;

	public NotificacaoEntity(Long id, UUID idUsuario, TipoNotificacao tipo, String chave, String titulo, String corpo,
			Map<String, String> dados, StatusNotificacao status, int tentativas, LocalDateTime proximaTentativaEm,
			LocalDateTime expiraEm, String ultimoErro, LocalDateTime criadoEm, LocalDateTime enviadaEm) {
		this.id = id;
		this.idUsuario = idUsuario;
		this.tipo = tipo;
		this.chave = chave;
		this.titulo = titulo;
		this.corpo = corpo;
		this.dados = dados;
		this.status = status;
		this.tentativas = tentativas;
		this.proximaTentativaEm = proximaTentativaEm;
		this.expiraEm = expiraEm;
		this.ultimoErro = ultimoErro;
		this.criadoEm = criadoEm;
		this.enviadaEm = enviadaEm;
	}

}

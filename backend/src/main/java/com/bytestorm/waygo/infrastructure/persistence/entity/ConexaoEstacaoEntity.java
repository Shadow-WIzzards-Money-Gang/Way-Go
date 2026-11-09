package com.bytestorm.waygo.infrastructure.persistence.entity;

import org.hibernate.type.NumericBooleanConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CONEXAO_ESTACAO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConexaoEstacaoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "ID_LINHA_ESTACAO_ORIGEM", nullable = false)
	private Long linhaEstacaoOrigemId;

	@Column(name = "ID_LINHA_ESTACAO_DESTINO", nullable = false)
	private Long linhaEstacaoDestinoId;

	@Column(name = "TEMPO_VIAGEM_SEGUNDOS")
	private Integer tempoViagemSegundos;

	@Column(name = "DISTANCIA_METROS")
	private Integer distanciaMetros;

	@Convert(converter = NumericBooleanConverter.class)
	@Column(name = "ATIVA", nullable = false)
	private boolean ativa;

}

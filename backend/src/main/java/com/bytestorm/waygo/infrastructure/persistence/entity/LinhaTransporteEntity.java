package com.bytestorm.waygo.infrastructure.persistence.entity;

import org.hibernate.type.NumericBooleanConverter;

import com.bytestorm.waygo.domain.enums.TipoTransporte;
import com.bytestorm.waygo.infrastructure.persistence.converter.TipoTransporteConverter;

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
@Table(name = "LINHA_TRANSPORTE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinhaTransporteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "NOME", nullable = false, length = 100)
	private String nome;

	@Column(name = "CODIGO", nullable = false, length = 20, unique = true)
	private String codigo;

	@Column(name = "COR", length = 20)
	private String cor;

	@Column(name = "OPERADORA", nullable = false, length = 100)
	private String operadora;

	@Convert(converter = TipoTransporteConverter.class)
	@Column(name = "TIPO_TRANSPORTE", nullable = false, length = 30)
	private TipoTransporte tipoTransporte;

	@Convert(converter = NumericBooleanConverter.class)
	@Column(name = "ATIVA", nullable = false)
	private boolean ativa;

	public LinhaTransporteEntity(Long id, String nome, String codigo, String cor, String operadora,
			TipoTransporte tipoTransporte, boolean ativa) {
		this.id = id;
		this.nome = nome;
		this.codigo = codigo;
		this.cor = cor;
		this.operadora = operadora;
		this.tipoTransporte = tipoTransporte;
		this.ativa = ativa;
	}

}

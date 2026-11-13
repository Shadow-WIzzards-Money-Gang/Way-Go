package com.bytestorm.waygo.infrastructure.persistence.entity;

import java.math.BigDecimal;

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
@Table(name = "ESTACAO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EstacaoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "NOME", nullable = false, length = 150)
	private String nome;

	@Column(name = "LATITUDE", precision = 10, scale = 7)
	private BigDecimal latitude;

	@Column(name = "LONGITUDE", precision = 10, scale = 7)
	private BigDecimal longitude;

	@Convert(converter = NumericBooleanConverter.class)
	@Column(name = "ATIVA", nullable = false)
	private boolean ativa;

	public EstacaoEntity(Long id, String nome, BigDecimal latitude, BigDecimal longitude, boolean ativa) {
		this.id = id;
		this.nome = nome;
		this.latitude = latitude;
		this.longitude = longitude;
		this.ativa = ativa;
	}

}

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
@Table(name = "LINHA_ESTACAO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LinhaEstacaoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "ID_LINHA", nullable = false)
	private Long linhaId;

	@Column(name = "ID_ESTACAO", nullable = false)
	private Long estacaoId;

	@Column(name = "SEQUENCIA", nullable = false)
	private Integer sequencia;

	@Convert(converter = NumericBooleanConverter.class)
	@Column(name = "ATIVA", nullable = false)
	private boolean ativa;

	public void alterarSequencia(int sequencia) {
		this.sequencia = sequencia;
	}

}

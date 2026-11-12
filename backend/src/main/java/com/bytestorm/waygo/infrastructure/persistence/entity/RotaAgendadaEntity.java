package com.bytestorm.waygo.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.type.NumericBooleanConverter;

import com.bytestorm.waygo.domain.enums.DiaSemana;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ROTA_AGENDADA")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RotaAgendadaEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "ID_ROTA_SALVA", nullable = false, updatable = false)
	private Long idRotaSalva;

	@Column(name = "HORARIO_PARTIDA_MINUTOS", nullable = false)
	private Integer horarioPartidaMinutos;

	@Column(name = "ANTECEDENCIA_MINUTOS", nullable = false)
	private Integer antecedenciaMinutos;

	@Convert(converter = NumericBooleanConverter.class)
	@Column(name = "ATIVA", nullable = false)
	private boolean ativa;

	@Column(name = "CRIADO_EM", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	// A coluna ID de AGENDAMENTO_DIA_SEMANA é IDENTITY, gerada pelo Oracle; por isso basta uma ElementCollection.
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "AGENDAMENTO_DIA_SEMANA", joinColumns = @JoinColumn(name = "ID_ROTA_AGENDADA"))
	@Enumerated(EnumType.STRING)
	@Column(name = "DIA", nullable = false, length = 10)
	private Set<DiaSemana> diasSemana = new HashSet<>();

	public RotaAgendadaEntity(Long id, Long idRotaSalva, Integer horarioPartidaMinutos, Integer antecedenciaMinutos,
			boolean ativa, LocalDateTime criadoEm, Set<DiaSemana> diasSemana) {
		this.id = id;
		this.idRotaSalva = idRotaSalva;
		this.horarioPartidaMinutos = horarioPartidaMinutos;
		this.antecedenciaMinutos = antecedenciaMinutos;
		this.ativa = ativa;
		this.criadoEm = criadoEm;
		this.diasSemana = new HashSet<>(diasSemana);
	}

}

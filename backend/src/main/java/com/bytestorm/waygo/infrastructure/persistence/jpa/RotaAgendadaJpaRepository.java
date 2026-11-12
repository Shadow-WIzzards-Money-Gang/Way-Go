package com.bytestorm.waygo.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bytestorm.waygo.domain.enums.DiaSemana;
import com.bytestorm.waygo.infrastructure.persistence.entity.RotaAgendadaEntity;

public interface RotaAgendadaJpaRepository extends JpaRepository<RotaAgendadaEntity, Long> {

	List<RotaAgendadaEntity> findByIdRotaSalvaOrderByHorarioPartidaMinutos(Long idRotaSalva);

	@Query("""
			select a from RotaAgendadaEntity a
			where a.idRotaSalva in (select r.id from RotaSalvaEntity r where r.idUsuario = :idUsuario)
			order by a.horarioPartidaMinutos
			""")
	List<RotaAgendadaEntity> findByUsuario(@Param("idUsuario") UUID idUsuario);

	@Query("""
			select a from RotaAgendadaEntity a
			where a.ativa = true and :dia member of a.diasSemana
			order by a.horarioPartidaMinutos
			""")
	List<RotaAgendadaEntity> findAtivasPorDia(@Param("dia") DiaSemana dia);

}

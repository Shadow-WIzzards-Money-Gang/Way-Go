package com.bytestorm.waygo.infrastructure.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaEstacaoEntity;

public interface LinhaEstacaoJpaRepository extends JpaRepository<LinhaEstacaoEntity, Long> {

	List<LinhaEstacaoEntity> findByLinhaIdOrderBySequenciaAsc(Long linhaId);

	List<LinhaEstacaoEntity> findByEstacaoId(Long estacaoId);

}

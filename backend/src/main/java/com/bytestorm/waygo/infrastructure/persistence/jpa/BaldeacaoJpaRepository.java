package com.bytestorm.waygo.infrastructure.persistence.jpa;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bytestorm.waygo.infrastructure.persistence.entity.BaldeacaoEntity;

public interface BaldeacaoJpaRepository extends JpaRepository<BaldeacaoEntity, Long> {

	List<BaldeacaoEntity> findByAtivaTrue();

	List<BaldeacaoEntity> findByLinhaEstacaoOrigemIdIn(Collection<Long> linhaEstacaoIds);

}

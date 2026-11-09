package com.bytestorm.waygo.infrastructure.persistence.jpa;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bytestorm.waygo.infrastructure.persistence.entity.ConexaoEstacaoEntity;

public interface ConexaoEstacaoJpaRepository extends JpaRepository<ConexaoEstacaoEntity, Long> {

	List<ConexaoEstacaoEntity> findByAtivaTrue();

	List<ConexaoEstacaoEntity> findByLinhaEstacaoOrigemIdIn(Collection<Long> linhaEstacaoIds);

}

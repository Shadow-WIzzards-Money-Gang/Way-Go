package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.bytestorm.waygo.domain.entity.ConexaoEstacao;
import com.bytestorm.waygo.domain.interfaces.ConexaoEstacaoRepository;
import com.bytestorm.waygo.infrastructure.persistence.jpa.ConexaoEstacaoJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.mapper.PersistenciaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ConexaoEstacaoRepositoryAdapter implements ConexaoEstacaoRepository {

	private final ConexaoEstacaoJpaRepository jpaRepository;

	@Override
	public List<ConexaoEstacao> listarAtivas() {
		return jpaRepository.findByAtivaTrue().stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

	@Override
	public List<ConexaoEstacao> listarPorOrigens(Collection<Long> linhaEstacaoIds) {
		return jpaRepository.findByLinhaEstacaoOrigemIdIn(linhaEstacaoIds).stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

}

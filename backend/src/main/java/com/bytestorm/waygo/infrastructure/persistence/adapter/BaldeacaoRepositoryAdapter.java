package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.bytestorm.waygo.domain.entity.Baldeacao;
import com.bytestorm.waygo.domain.interfaces.BaldeacaoRepository;
import com.bytestorm.waygo.infrastructure.persistence.jpa.BaldeacaoJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.mapper.PersistenciaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BaldeacaoRepositoryAdapter implements BaldeacaoRepository {

	private final BaldeacaoJpaRepository jpaRepository;

	@Override
	public List<Baldeacao> listarAtivas() {
		return jpaRepository.findByAtivaTrue().stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

	@Override
	public List<Baldeacao> listarPorOrigens(Collection<Long> linhaEstacaoIds) {
		return jpaRepository.findByLinhaEstacaoOrigemIdIn(linhaEstacaoIds).stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

}

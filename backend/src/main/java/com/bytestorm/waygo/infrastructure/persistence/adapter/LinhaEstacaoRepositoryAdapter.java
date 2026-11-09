package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.interfaces.LinhaEstacaoRepository;
import com.bytestorm.waygo.infrastructure.persistence.jpa.LinhaEstacaoJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.mapper.PersistenciaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LinhaEstacaoRepositoryAdapter implements LinhaEstacaoRepository {

	private final LinhaEstacaoJpaRepository jpaRepository;

	@Override
	public List<LinhaEstacao> listarPorLinhaOrdenadoPorSequencia(Long linhaId) {
		return jpaRepository.findByLinhaIdOrderBySequenciaAsc(linhaId).stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

	@Override
	public List<LinhaEstacao> listarPorEstacao(Long estacaoId) {
		return jpaRepository.findByEstacaoId(estacaoId).stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

	@Override
	public List<LinhaEstacao> buscarPorIds(Collection<Long> ids) {
		return jpaRepository.findAllById(ids).stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

}

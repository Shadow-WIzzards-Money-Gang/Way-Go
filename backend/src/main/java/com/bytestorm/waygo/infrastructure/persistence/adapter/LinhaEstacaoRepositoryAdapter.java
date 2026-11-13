package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.interfaces.LinhaEstacaoRepository;
import com.bytestorm.waygo.infrastructure.persistence.entity.LinhaEstacaoEntity;
import com.bytestorm.waygo.infrastructure.persistence.jpa.LinhaEstacaoJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.mapper.PersistenciaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LinhaEstacaoRepositoryAdapter implements LinhaEstacaoRepository {

	private static final int SEQUENCIA_TEMPORARIA = 50_000;

	private final LinhaEstacaoJpaRepository jpaRepository;

	@Override
	public List<LinhaEstacao> listarTodas() {
		return jpaRepository.findAll().stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

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

	@Override
	@Transactional
	public void salvarSequencias(List<LinhaEstacao> linhaEstacoes) {
		Map<Long, Integer> sequencias = linhaEstacoes.stream()
				.collect(Collectors.toMap(LinhaEstacao::getId, LinhaEstacao::getSequencia));
		List<LinhaEstacaoEntity> entities = jpaRepository.findAllById(sequencias.keySet());

		entities.forEach(e -> e.alterarSequencia(SEQUENCIA_TEMPORARIA + sequencias.get(e.getId())));
		jpaRepository.flush();
		entities.forEach(e -> e.alterarSequencia(sequencias.get(e.getId())));
		jpaRepository.flush();
	}

}

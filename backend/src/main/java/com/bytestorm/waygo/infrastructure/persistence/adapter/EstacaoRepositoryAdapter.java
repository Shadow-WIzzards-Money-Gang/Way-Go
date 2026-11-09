package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.infrastructure.persistence.jpa.EstacaoJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.mapper.PersistenciaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class EstacaoRepositoryAdapter implements EstacaoRepository {

	private final EstacaoJpaRepository jpaRepository;

	@Override
	public List<Estacao> listarTodas() {
		return jpaRepository.findAll(Sort.by("nome")).stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

	@Override
	public Optional<Estacao> buscarPorId(Long id) {
		return jpaRepository.findById(id).map(PersistenciaMapper::paraDominio);
	}

	@Override
	public List<Estacao> buscarPorIds(Collection<Long> ids) {
		return jpaRepository.findAllById(ids).stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

	@Override
	public boolean existePorId(Long id) {
		return jpaRepository.existsById(id);
	}

}

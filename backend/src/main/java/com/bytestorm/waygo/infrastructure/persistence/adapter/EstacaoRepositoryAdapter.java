package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.domain.valueobject.Pagina;
import com.bytestorm.waygo.infrastructure.persistence.entity.EstacaoEntity;
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
	public Pagina<Estacao> listarPaginado(String busca, boolean somenteIntegracoes, int pagina, int tamanho) {
		PageRequest pageRequest = PageRequest.of(pagina, tamanho, Sort.by("nome").and(Sort.by("id")));
		Page<EstacaoEntity> resultado = jpaRepository.buscarPaginado(
				busca == null || busca.isBlank() ? null : busca,
				somenteIntegracoes ? 2 : 0,
				pageRequest);
		return new Pagina<>(
				resultado.map(PersistenciaMapper::paraDominio).getContent(),
				resultado.getNumber(),
				resultado.getSize(),
				resultado.getTotalElements());
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

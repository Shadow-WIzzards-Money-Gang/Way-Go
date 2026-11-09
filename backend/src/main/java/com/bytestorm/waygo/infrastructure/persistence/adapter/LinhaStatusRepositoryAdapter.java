package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.bytestorm.waygo.domain.entity.LinhaStatus;
import com.bytestorm.waygo.domain.interfaces.LinhaStatusRepository;
import com.bytestorm.waygo.infrastructure.persistence.jpa.LinhaStatusJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.mapper.PersistenciaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LinhaStatusRepositoryAdapter implements LinhaStatusRepository {

	private final LinhaStatusJpaRepository jpaRepository;

	@Override
	public Optional<LinhaStatus> buscarAtualPorLinha(Long linhaId) {
		return jpaRepository.findFirstByLinhaIdOrderByIdDesc(linhaId)
				.map(PersistenciaMapper::paraDominio);
	}

	@Override
	public List<LinhaStatus> buscarAtuaisDeTodasAsLinhas() {
		return jpaRepository.findMaisRecentePorLinha().stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

	@Override
	public LinhaStatus salvar(LinhaStatus linhaStatus) {
		return PersistenciaMapper.paraDominio(jpaRepository.save(PersistenciaMapper.paraEntity(linhaStatus)));
	}

}

package com.bytestorm.waygo.infrastructure.persistence.adapter;

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
		return jpaRepository.findFirstByLinhaIdOrderByAtualizadoEmDescIdDesc(linhaId)
				.map(PersistenciaMapper::paraDominio);
	}

}

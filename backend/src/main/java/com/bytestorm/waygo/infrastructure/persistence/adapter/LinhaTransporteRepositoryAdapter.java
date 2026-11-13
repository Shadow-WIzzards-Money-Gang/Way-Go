package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.interfaces.LinhaTransporteRepository;
import com.bytestorm.waygo.infrastructure.persistence.jpa.LinhaTransporteJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.mapper.PersistenciaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class LinhaTransporteRepositoryAdapter implements LinhaTransporteRepository {

	private final LinhaTransporteJpaRepository jpaRepository;

	@Override
	public List<LinhaTransporte> listarTodas() {
		return jpaRepository.findAll(Sort.by("id")).stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

	@Override
	public Optional<LinhaTransporte> buscarPorId(Long id) {
		return jpaRepository.findById(id).map(PersistenciaMapper::paraDominio);
	}

	@Override
	public List<LinhaTransporte> buscarPorIds(Collection<Long> ids) {
		return jpaRepository.findAllById(ids).stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

	@Override
	public boolean existePorId(Long id) {
		return jpaRepository.existsById(id);
	}

	@Override
	public boolean existePorCodigo(String codigo) {
		return codigo != null && !codigo.isBlank() && jpaRepository.existsByCodigoIgnoreCase(codigo.trim());
	}

	@Override
	public LinhaTransporte salvar(LinhaTransporte linha) {
		return PersistenciaMapper.paraDominio(jpaRepository.save(PersistenciaMapper.paraEntity(linha)));
	}

}

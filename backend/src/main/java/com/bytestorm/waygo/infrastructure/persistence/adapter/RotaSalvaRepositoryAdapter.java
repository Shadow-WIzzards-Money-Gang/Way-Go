package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.domain.entity.RotaSalva;
import com.bytestorm.waygo.domain.interfaces.RotaSalvaRepository;
import com.bytestorm.waygo.infrastructure.persistence.jpa.RotaSalvaJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.mapper.PersistenciaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RotaSalvaRepositoryAdapter implements RotaSalvaRepository {

	private final RotaSalvaJpaRepository jpaRepository;

	@Override
	@Transactional
	public RotaSalva salvar(RotaSalva rotaSalva) {
		return PersistenciaMapper.paraDominio(jpaRepository.save(PersistenciaMapper.paraEntity(rotaSalva)));
	}

	@Override
	public Optional<RotaSalva> buscarPorId(Long id) {
		return jpaRepository.findById(id).map(PersistenciaMapper::paraDominio);
	}

	@Override
	public Optional<RotaSalva> buscarPorIdEUsuario(Long id, UUID idUsuario) {
		return jpaRepository.findByIdAndIdUsuario(id, idUsuario).map(PersistenciaMapper::paraDominio);
	}

	@Override
	public List<RotaSalva> listarPorUsuario(UUID idUsuario) {
		return jpaRepository.findByIdUsuarioOrderByCriadoEmDesc(idUsuario).stream()
				.map(PersistenciaMapper::paraDominio)
				.toList();
	}

	// Os agendamentos e seus dias são removidos pelo ON DELETE CASCADE da V6.
	@Override
	@Transactional
	public void excluir(Long id) {
		jpaRepository.deleteById(id);
	}

}

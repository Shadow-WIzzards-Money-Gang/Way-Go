package com.bytestorm.waygo.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.domain.entity.RotaAgendada;
import com.bytestorm.waygo.domain.enums.DiaSemana;
import com.bytestorm.waygo.domain.interfaces.RotaAgendadaRepository;
import com.bytestorm.waygo.infrastructure.persistence.entity.RotaAgendadaEntity;
import com.bytestorm.waygo.infrastructure.persistence.jpa.RotaAgendadaJpaRepository;
import com.bytestorm.waygo.infrastructure.persistence.mapper.PersistenciaMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RotaAgendadaRepositoryAdapter implements RotaAgendadaRepository {

	private final RotaAgendadaJpaRepository jpaRepository;

	@Override
	@Transactional
	public RotaAgendada salvar(RotaAgendada rotaAgendada) {
		return PersistenciaMapper.paraDominio(jpaRepository.save(PersistenciaMapper.paraEntity(rotaAgendada)));
	}

	@Override
	public Optional<RotaAgendada> buscarPorId(Long id) {
		return jpaRepository.findById(id).map(PersistenciaMapper::paraDominio);
	}

	@Override
	public List<RotaAgendada> listarPorRotaSalva(Long idRotaSalva) {
		return paraDominio(jpaRepository.findByIdRotaSalvaOrderByHorarioPartidaMinutos(idRotaSalva));
	}

	@Override
	public List<RotaAgendada> listarPorUsuario(UUID idUsuario) {
		return paraDominio(jpaRepository.findByUsuario(idUsuario));
	}

	@Override
	public List<RotaAgendada> listarAtivasPorDia(DiaSemana diaSemana) {
		return paraDominio(jpaRepository.findAtivasPorDia(diaSemana));
	}

	@Override
	@Transactional
	public void excluir(Long id) {
		jpaRepository.deleteById(id);
	}

	private static List<RotaAgendada> paraDominio(List<RotaAgendadaEntity> entities) {
		return entities.stream().map(PersistenciaMapper::paraDominio).toList();
	}

}

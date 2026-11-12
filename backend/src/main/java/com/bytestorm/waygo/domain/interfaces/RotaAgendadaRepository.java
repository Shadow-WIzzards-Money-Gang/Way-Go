package com.bytestorm.waygo.domain.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.bytestorm.waygo.domain.entity.RotaAgendada;
import com.bytestorm.waygo.domain.enums.DiaSemana;

public interface RotaAgendadaRepository {

	RotaAgendada salvar(RotaAgendada rotaAgendada);

	Optional<RotaAgendada> buscarPorId(Long id);

	List<RotaAgendada> listarPorRotaSalva(Long idRotaSalva);

	List<RotaAgendada> listarPorUsuario(UUID idUsuario);

	List<RotaAgendada> listarAtivasPorDia(DiaSemana diaSemana);

	void excluir(Long id);

}

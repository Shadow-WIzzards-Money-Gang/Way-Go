package com.bytestorm.waygo.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.bytestorm.waygo.application.dto.request.RotaAgendadaRequest;
import com.bytestorm.waygo.application.dto.response.RotaAgendadaResponse;
import com.bytestorm.waygo.domain.entity.RotaAgendada;
import com.bytestorm.waygo.domain.entity.RotaSalva;
import com.bytestorm.waygo.domain.enums.DiaSemana;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.RotaAgendadaRepository;
import com.bytestorm.waygo.domain.interfaces.RotaSalvaRepository;
import com.bytestorm.waygo.domain.util.Horarios;

@Service
public class RotaAgendadaService {

	static final int ANTECEDENCIA_PADRAO = 5;

	private final RotaAgendadaRepository agendamentoRepository;
	private final RotaSalvaRepository rotaSalvaRepository;
	private final Clock clock;

	public RotaAgendadaService(RotaAgendadaRepository agendamentoRepository, RotaSalvaRepository rotaSalvaRepository,
			Clock clock) {
		this.agendamentoRepository = agendamentoRepository;
		this.rotaSalvaRepository = rotaSalvaRepository;
		this.clock = clock;
	}

	public List<RotaAgendadaResponse> listar(UUID idUsuario) {
		return agendamentoRepository.listarPorUsuario(idUsuario).stream()
				.map(RotaAgendadaResponse::de)
				.toList();
	}

	public List<RotaAgendadaResponse> listarDaRota(UUID idUsuario, Long idRota) {
		RotaSalva rota = buscarRotaDoUsuario(idUsuario, idRota);
		return agendamentoRepository.listarPorRotaSalva(rota.getId()).stream()
				.map(RotaAgendadaResponse::de)
				.toList();
	}

	public RotaAgendadaResponse buscar(UUID idUsuario, Long id) {
		return RotaAgendadaResponse.de(buscarAgendamentoDoUsuario(idUsuario, id));
	}

	public RotaAgendadaResponse criar(UUID idUsuario, Long idRota, RotaAgendadaRequest request) {
		RotaSalva rota = buscarRotaDoUsuario(idUsuario, idRota);
		int antecedencia = request.antecedenciaMinutos() != null ? request.antecedenciaMinutos() : ANTECEDENCIA_PADRAO;
		boolean ativa = request.ativa() == null || request.ativa();

		RotaAgendada novo = validando(() -> RotaAgendada.reconstruir(null, rota.getId(),
				Horarios.paraMinutos(request.horario()), antecedencia, ativa, converterDias(request.diasSemana()),
				LocalDateTime.now(clock)));
		return RotaAgendadaResponse.de(agendamentoRepository.salvar(novo));
	}

	public RotaAgendadaResponse atualizar(UUID idUsuario, Long id, RotaAgendadaRequest request) {
		RotaAgendada atual = buscarAgendamentoDoUsuario(idUsuario, id);
		int antecedencia = request.antecedenciaMinutos() != null
				? request.antecedenciaMinutos()
				: atual.getAntecedenciaMinutos();
		boolean ativa = request.ativa() != null ? request.ativa() : atual.isAtiva();

		RotaAgendada atualizado = validando(() -> RotaAgendada.reconstruir(atual.getId(), atual.getIdRotaSalva(),
				Horarios.paraMinutos(request.horario()), antecedencia, ativa, converterDias(request.diasSemana()),
				atual.getCriadoEm()));
		return RotaAgendadaResponse.de(agendamentoRepository.salvar(atualizado));
	}

	public void excluir(UUID idUsuario, Long id) {
		RotaAgendada agendamento = buscarAgendamentoDoUsuario(idUsuario, id);
		agendamentoRepository.excluir(agendamento.getId());
	}

	private RotaSalva buscarRotaDoUsuario(UUID idUsuario, Long idRota) {
		return rotaSalvaRepository.buscarPorIdEUsuario(idRota, idUsuario)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Rota salva não encontrada: " + idRota));
	}

	private RotaAgendada buscarAgendamentoDoUsuario(UUID idUsuario, Long id) {
		return agendamentoRepository.buscarPorId(id)
				.filter(a -> rotaSalvaRepository.buscarPorIdEUsuario(a.getIdRotaSalva(), idUsuario).isPresent())
				.orElseThrow(() -> new RecursoNaoEncontradoException("Agendamento não encontrado: " + id));
	}

	private static Set<DiaSemana> converterDias(List<String> dias) {
		if (dias == null || dias.isEmpty()) {
			throw new IllegalArgumentException("Escolha ao menos um dia da semana");
		}
		Set<DiaSemana> convertidos = EnumSet.noneOf(DiaSemana.class);
		for (String dia : dias) {
			convertidos.add(DiaSemana.de(dia));
		}
		return convertidos;
	}

	private static RotaAgendada validando(Supplier<RotaAgendada> construcao) {
		try {
			return construcao.get();
		} catch (IllegalArgumentException | IllegalStateException e) {
			throw new RequisicaoInvalidaException(e.getMessage());
		}
	}

}

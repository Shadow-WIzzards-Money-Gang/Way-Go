package com.bytestorm.waygo.application.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.bytestorm.waygo.application.cache.StatusLinhasCache;
import com.bytestorm.waygo.application.dto.response.LinhaResumoResponse;
import com.bytestorm.waygo.application.dto.response.RotaResponse;
import com.bytestorm.waygo.application.dto.response.TrechoRotaResponse;
import com.bytestorm.waygo.application.event.StatusLinhaAlteradoEvent;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.entity.Notificacao;
import com.bytestorm.waygo.domain.entity.RotaAgendada;
import com.bytestorm.waygo.domain.entity.RotaSalva;
import com.bytestorm.waygo.domain.enums.DiaSemana;
import com.bytestorm.waygo.domain.enums.StatusOperacional;
import com.bytestorm.waygo.domain.enums.TipoNotificacao;
import com.bytestorm.waygo.domain.interfaces.DispositivoUsuarioRepository;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaTransporteRepository;
import com.bytestorm.waygo.domain.interfaces.NotificacaoRepository;
import com.bytestorm.waygo.domain.interfaces.RotaAgendadaRepository;
import com.bytestorm.waygo.domain.interfaces.RotaSalvaRepository;
import com.bytestorm.waygo.domain.util.Horarios;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoRotaService {

	static final Duration TOLERANCIA_SEM_ANTECEDENCIA = Duration.ofMinutes(5);
	static final Duration VALIDADE_ALERTA_STATUS = Duration.ofHours(1);

	private final RotaAgendadaRepository agendamentoRepository;
	private final RotaSalvaRepository rotaSalvaRepository;
	private final NotificacaoRepository notificacaoRepository;
	private final DispositivoUsuarioRepository dispositivoRepository;
	private final LinhaTransporteRepository linhaRepository;
	private final EstacaoRepository estacaoRepository;
	private final StatusLinhasCache statusCache;
	private final RotaService rotaService;
	private final Clock clock;
	
	public int enfileirarLembretes() {
		LocalDateTime agora = LocalDateTime.now(clock);
		int criados = 0;
		for (LocalDate dataPartida : List.of(agora.toLocalDate(), agora.toLocalDate().plusDays(1))) {
			DiaSemana dia = DiaSemana.de(dataPartida.getDayOfWeek());
			for (RotaAgendada agendamento : agendamentoRepository.listarAtivasPorDia(dia)) {
				if (enfileirarLembrete(agendamento, dataPartida, agora)) {
					criados++;
				}
			}
		}
		if (criados > 0) {
			log.info("{} lembrete(s) de agendamento colocados na fila", criados);
		}
		return criados;
	}

	private boolean enfileirarLembrete(RotaAgendada agendamento, LocalDate dataPartida, LocalDateTime agora) {
		LocalDateTime partida = dataPartida.atStartOfDay().plusMinutes(agendamento.getHorarioPartidaMinutos());
		LocalDateTime aviso = partida.minusMinutes(agendamento.getAntecedenciaMinutos());
		LocalDateTime fim = agendamento.getAntecedenciaMinutos() == 0
				? partida.plus(TOLERANCIA_SEM_ANTECEDENCIA)
				: partida;
		if (agora.isBefore(aviso) || !agora.isBefore(fim)) {
			return false;
		}

		Optional<RotaSalva> rota = rotaSalvaRepository.buscarPorId(agendamento.getIdRotaSalva());
		if (rota.isEmpty()) {
			return false;
		}
		UUID idUsuario = rota.get().getIdUsuario();
		String chave = "agendamento:" + agendamento.getId() + ":" + dataPartida;
		if (notificacaoRepository.existe(idUsuario, chave)) {
			return false;
		}

		Mensagem mensagem = mensagemLembrete(rota.get(), Horarios.formatar(agendamento.getHorarioPartidaMinutos()));
		return notificacaoRepository.salvarSeNova(Notificacao.criar(idUsuario, TipoNotificacao.LEMBRETE_AGENDAMENTO,
				chave, mensagem.titulo(), mensagem.corpo(), mensagem.dados(), fim, agora)).isPresent();
	}

	Mensagem mensagemLembrete(RotaSalva rota, String horario) {
		Map<Long, StatusOperacional> status = statusCache.statusAtuais();
		Optional<RotaResponse> habitual = rotaService.rotaHabitual(rota);
		if (habitual.isEmpty()) {
			return new Mensagem(rota.getNome() + ": confira a rota",
					"Não encontramos um trajeto para esta rota agora. Confira no app antes de sair às " + horario + ".",
					dados(rota, TipoNotificacao.LEMBRETE_AGENDAMENTO, false));
		}

		List<String> problemas = problemas(habitual.get(), status);
		if (problemas.isEmpty()) {
			RotaResponse atual = rotaService.rotaAgora(rota, false).orElse(habitual.get());
			return new Mensagem(rota.getNome() + ": tudo certo",
					"Saída às " + horario + ". Trajeto de ~" + atual.tempoEstimadoMinutos() + " min por "
							+ linhasDa(atual) + ", sem problemas nas linhas.",
					dados(rota, TipoNotificacao.LEMBRETE_AGENDAMENTO, false));
		}

		String titulo = "Atenção na rota " + rota.getNome();
		String inicio = String.join("; ", problemas) + ".";
		Optional<RotaResponse> alternativa = rotaService.rotaAgora(rota, true);
		if (alternativa.isPresent()) {
			return new Mensagem(titulo,
					inicio + " Rota alternativa: ~" + alternativa.get().tempoEstimadoMinutos() + " min por "
							+ linhasDa(alternativa.get()) + ". Saída às " + horario + ".",
					dados(rota, TipoNotificacao.LEMBRETE_AGENDAMENTO, true));
		}
		Optional<RotaResponse> atual = rotaService.rotaAgora(rota, false);
		String complemento = atual
				.map(r -> " Não há rota só com linhas normais; o trajeto deve levar ~" + r.tempoEstimadoMinutos()
						+ " min.")
				.orElse(" No momento não há rota disponível.");
		return new Mensagem(titulo, inicio + complemento + " Saída às " + horario + ".",
				dados(rota, TipoNotificacao.LEMBRETE_AGENDAMENTO, false));
	}

	public int notificarMudancaStatus(StatusLinhaAlteradoEvent evento) {
		if (!geraAlerta(evento)) {
			return 0;
		}
		Set<UUID> interessados = dispositivoRepository.listarUsuariosComAlertasStatus();
		if (interessados.isEmpty()) {
			return 0;
		}

		LocalDateTime agora = LocalDateTime.now(clock);
		String nomeLinha = linhaRepository.buscarPorId(evento.linhaId()).map(LinhaTransporte::getNome)
				.orElse("Linha " + evento.linhaId());
		boolean problema = !evento.novo().operacaoNormal();
		String chave = "status:" + evento.idLinhaStatus();

		Map<UUID, List<RotaSalva>> rotasPorUsuario = rotaSalvaRepository.listarTodas().stream()
				.filter(r -> interessados.contains(r.getIdUsuario()))
				.collect(Collectors.groupingBy(RotaSalva::getIdUsuario, LinkedHashMap::new, Collectors.toList()));
		Map<String, Optional<RotaResponse>> habituais = new HashMap<>();

		int criados = 0;
		for (Map.Entry<UUID, List<RotaSalva>> entrada : rotasPorUsuario.entrySet()) {
			List<RotaSalva> afetadas = entrada.getValue().stream()
					.filter(r -> habituais.computeIfAbsent(chaveTrajeto(r), k -> rotaService.rotaHabitual(r))
							.map(rota -> usaLinha(rota, evento.linhaId()))
							.orElse(false))
					.toList();
			if (afetadas.isEmpty() || notificacaoRepository.existe(entrada.getKey(), chave)) {
				continue;
			}
			Mensagem mensagem = mensagemStatus(evento, nomeLinha, problema, afetadas);
			if (notificacaoRepository.salvarSeNova(Notificacao.criar(entrada.getKey(), TipoNotificacao.STATUS_LINHA,
					chave, mensagem.titulo(), mensagem.corpo(), mensagem.dados(),
					agora.plus(VALIDADE_ALERTA_STATUS), agora)).isPresent()) {
				criados++;
			}
		}
		log.info("Mudança de status de {} ({} -> {}): {} alerta(s) na fila", nomeLinha, evento.anterior(),
				evento.novo(), criados);
		return criados;
	}

	private static boolean geraAlerta(StatusLinhaAlteradoEvent evento) {
		if (evento.anterior() == null || evento.anterior() == evento.novo()
				|| ignorado(evento.anterior()) || ignorado(evento.novo())) {
			return false;
		}
		// Só interessa entrar ou sair de problema (ou trocar de um problema para outro).
		return !(evento.anterior().operacaoNormal() && evento.novo().operacaoNormal());
	}

	private static boolean ignorado(StatusOperacional status) {
		return status == StatusOperacional.OPERACAO_ENCERRADA || status == StatusOperacional.DESCONHECIDO;
	}

	private Mensagem mensagemStatus(StatusLinhaAlteradoEvent evento, String nomeLinha, boolean problema,
			List<RotaSalva> afetadas) {
		String rotas = juntar(afetadas.stream().map(RotaSalva::getNome).toList());
		RotaSalva principal = afetadas.getFirst();
		if (!problema) {
			return new Mensagem(nomeLinha + ": operação normal",
					nomeLinha + " voltou a operar normalmente. Afeta " + rotas + ".",
					dados(principal, TipoNotificacao.STATUS_LINHA, false));
		}

		StringBuilder corpo = new StringBuilder("Afeta ").append(rotas).append('.');
		if (evento.descricao() != null && !evento.descricao().isBlank()) {
			corpo.append(' ').append(evento.descricao().trim());
			if (!evento.descricao().trim().endsWith(".")) {
				corpo.append('.');
			}
		}
		Optional<RotaResponse> alternativa = rotaService.rotaAgora(principal, true);
		alternativa.ifPresentOrElse(
				alt -> corpo.append(" Rota alternativa para ").append(principal.getNome()).append(": ~")
						.append(alt.tempoEstimadoMinutos()).append(" min por ").append(linhasDa(alt)).append('.'),
				() -> corpo.append(" Não há rota alternativa só com linhas normais no momento."));
		return new Mensagem(nomeLinha + ": " + evento.novo().getDescricao(), corpo.toString(),
				dados(principal, TipoNotificacao.STATUS_LINHA, alternativa.isPresent()));
	}

	private Map<String, String> dados(RotaSalva rota, TipoNotificacao tipo, boolean alternativa) {
		Map<String, String> dados = new LinkedHashMap<>();
		dados.put("tipo", tipo.name());
		dados.put("idRotaSalva", String.valueOf(rota.getId()));
		dados.put("origemId", String.valueOf(rota.getIdEstacaoOrigem()));
		dados.put("destinoId", String.valueOf(rota.getIdEstacaoDestino()));
		estacaoRepository.buscarPorId(rota.getIdEstacaoOrigem())
				.ifPresent(e -> dados.put("origemNome", e.getNome()));
		estacaoRepository.buscarPorId(rota.getIdEstacaoDestino())
				.ifPresent(e -> dados.put("destinoNome", e.getNome()));
		dados.put("criterio", rota.getCriterioRota().name());
		dados.put("alternativa", String.valueOf(alternativa));
		return dados;
	}

	private static List<String> problemas(RotaResponse rota, Map<Long, StatusOperacional> status) {
		List<String> problemas = new ArrayList<>();
		for (LinhaResumoResponse linha : linhasUsadas(rota)) {
			StatusOperacional atual = status.getOrDefault(linha.id(), StatusOperacional.NORMAL);
			if (!atual.operacaoNormal()) {
				problemas.add(linha.nome() + ": " + atual.getDescricao());
			}
		}
		return problemas;
	}

	private static boolean usaLinha(RotaResponse rota, Long linhaId) {
		return rota.trechos().stream().anyMatch(t -> Objects.equals(t.linha().id(), linhaId));
	}

	private static List<LinhaResumoResponse> linhasUsadas(RotaResponse rota) {
		Map<Long, LinhaResumoResponse> porId = new LinkedHashMap<>();
		for (TrechoRotaResponse trecho : rota.trechos()) {
			porId.putIfAbsent(trecho.linha().id(), trecho.linha());
		}
		return List.copyOf(porId.values());
	}

	private static String linhasDa(RotaResponse rota) {
		return juntar(linhasUsadas(rota).stream().map(LinhaResumoResponse::nome).toList());
	}

	static String juntar(List<String> itens) {
		if (itens.size() <= 1) {
			return itens.isEmpty() ? "" : itens.getFirst();
		}
		return String.join(", ", itens.subList(0, itens.size() - 1)) + " e " + itens.getLast();
	}

	private static String chaveTrajeto(RotaSalva rota) {
		return rota.getIdEstacaoOrigem() + "-" + rota.getIdEstacaoDestino() + "-" + rota.getCriterioRota();
	}

	record Mensagem(String titulo, String corpo, Map<String, String> dados) {
	}

}

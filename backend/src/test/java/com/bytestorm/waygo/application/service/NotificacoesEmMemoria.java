package com.bytestorm.waygo.application.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.bytestorm.waygo.domain.entity.DispositivoUsuario;
import com.bytestorm.waygo.domain.entity.Notificacao;
import com.bytestorm.waygo.domain.entity.RotaAgendada;
import com.bytestorm.waygo.domain.entity.RotaSalva;
import com.bytestorm.waygo.domain.enums.DiaSemana;
import com.bytestorm.waygo.domain.interfaces.DispositivoUsuarioRepository;
import com.bytestorm.waygo.domain.interfaces.NotificacaoRepository;
import com.bytestorm.waygo.domain.interfaces.RotaAgendadaRepository;
import com.bytestorm.waygo.domain.interfaces.RotaSalvaRepository;

/** Repositórios em memória das notificações, aparelhos, rotas salvas e agendamentos. */
final class NotificacoesEmMemoria {

	private NotificacoesEmMemoria() {
	}

	/** Relógio que os testes podem avançar. */
	static final class Relogio extends Clock {

		private Instant instante;

		Relogio(LocalDateTime inicio) {
			this.instante = inicio.toInstant(ZoneOffset.UTC);
		}

		void avancar(Duration duracao) {
			instante = instante.plus(duracao);
		}

		void definir(LocalDateTime momento) {
			instante = momento.toInstant(ZoneOffset.UTC);
		}

		LocalDateTime agora() {
			return LocalDateTime.ofInstant(instante, ZoneOffset.UTC);
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instant instant() {
			return instante;
		}
	}

	/** Guarda cópias, como o banco, e respeita a chave única (usuário, chave). */
	static final class Notificacoes implements NotificacaoRepository {

		private final Map<Long, Notificacao> porId = new LinkedHashMap<>();
		private long sequencia;

		List<Notificacao> todas() {
			return porId.values().stream().map(Notificacoes::copia).toList();
		}

		Notificacao unica() {
			if (porId.size() != 1) {
				throw new AssertionError("esperava 1 notificação, mas há " + porId.size());
			}
			return todas().getFirst();
		}

		@Override
		public Optional<Notificacao> salvarSeNova(Notificacao notificacao) {
			return existe(notificacao.getIdUsuario(), notificacao.getChave())
					? Optional.empty()
					: Optional.of(salvar(notificacao));
		}

		@Override
		public boolean existe(UUID idUsuario, String chave) {
			return porId.values().stream()
					.anyMatch(n -> n.getIdUsuario().equals(idUsuario) && n.getChave().equals(chave));
		}

		@Override
		public List<Notificacao> listarProntasParaEnvio(LocalDateTime agora, int limite) {
			return porId.values().stream()
					.filter(Notificacao::estaPendente)
					.filter(n -> !n.getProximaTentativaEm().isAfter(agora))
					.sorted(Comparator.comparing(Notificacao::getProximaTentativaEm))
					.limit(limite)
					.map(Notificacoes::copia)
					.toList();
		}

		@Override
		public Notificacao salvar(Notificacao n) {
			Long id = n.getId() != null ? n.getId() : ++sequencia;
			Notificacao salva = Notificacao.reconstruir(id, n.getIdUsuario(), n.getTipo(), n.getChave(),
					n.getTitulo(), n.getCorpo(), n.getDados(), n.getStatus(), n.getTentativas(),
					n.getProximaTentativaEm(), n.getExpiraEm(), n.getUltimoErro(), n.getCriadoEm(), n.getEnviadaEm());
			porId.put(id, salva);
			return copia(salva);
		}

		private static Notificacao copia(Notificacao n) {
			return Notificacao.reconstruir(n.getId(), n.getIdUsuario(), n.getTipo(), n.getChave(), n.getTitulo(),
					n.getCorpo(), n.getDados(), n.getStatus(), n.getTentativas(), n.getProximaTentativaEm(),
					n.getExpiraEm(), n.getUltimoErro(), n.getCriadoEm(), n.getEnviadaEm());
		}
	}

	static final class Dispositivos implements DispositivoUsuarioRepository {

		private final Map<Long, DispositivoUsuario> porId = new LinkedHashMap<>();
		private long sequencia;

		List<DispositivoUsuario> todos() {
			return porId.values().stream().map(Dispositivos::copia).toList();
		}

		@Override
		public DispositivoUsuario salvar(DispositivoUsuario d) {
			Long id = d.getId() != null ? d.getId() : ++sequencia;
			DispositivoUsuario salvo = DispositivoUsuario.reconstruir(id, d.getIdUsuario(), d.getTokenFcm(),
					d.getPlataforma(), d.isAlertasStatus(), d.isAtivo(), d.getCriadoEm(), d.getAtualizadoEm());
			porId.put(id, salvo);
			return copia(salvo);
		}

		@Override
		public Optional<DispositivoUsuario> buscarPorToken(String tokenFcm) {
			return porId.values().stream().filter(d -> d.getTokenFcm().equals(tokenFcm)).findFirst()
					.map(Dispositivos::copia);
		}

		@Override
		public List<DispositivoUsuario> listarAtivosDoUsuario(UUID idUsuario) {
			return porId.values().stream().filter(d -> d.isAtivo() && d.pertenceA(idUsuario))
					.map(Dispositivos::copia).toList();
		}

		@Override
		public Set<UUID> listarUsuariosComAlertasStatus() {
			return porId.values().stream().filter(d -> d.isAtivo() && d.isAlertasStatus())
					.map(DispositivoUsuario::getIdUsuario).collect(Collectors.toSet());
		}

		@Override
		public void excluirPorTokenEUsuario(String tokenFcm, UUID idUsuario) {
			porId.values().removeIf(d -> d.getTokenFcm().equals(tokenFcm) && d.pertenceA(idUsuario));
		}

		private static DispositivoUsuario copia(DispositivoUsuario d) {
			return DispositivoUsuario.reconstruir(d.getId(), d.getIdUsuario(), d.getTokenFcm(), d.getPlataforma(),
					d.isAlertasStatus(), d.isAtivo(), d.getCriadoEm(), d.getAtualizadoEm());
		}
	}

	static final class RotasSalvas implements RotaSalvaRepository {

		private final Map<Long, RotaSalva> porId = new LinkedHashMap<>();
		private long sequencia;

		@Override
		public RotaSalva salvar(RotaSalva r) {
			Long id = r.getId() != null ? r.getId() : ++sequencia;
			RotaSalva salva = RotaSalva.reconstruir(id, r.getIdEstacaoOrigem(), r.getIdEstacaoDestino(), r.getNome(),
					r.getCriterioRota(), r.getIdUsuario(), r.getCriadoEm());
			porId.put(id, salva);
			return salva;
		}

		@Override
		public Optional<RotaSalva> buscarPorId(Long id) {
			return Optional.ofNullable(porId.get(id));
		}

		@Override
		public Optional<RotaSalva> buscarPorIdEUsuario(Long id, UUID idUsuario) {
			return buscarPorId(id).filter(r -> r.getIdUsuario().equals(idUsuario));
		}

		@Override
		public List<RotaSalva> listarPorUsuario(UUID idUsuario) {
			return porId.values().stream().filter(r -> r.getIdUsuario().equals(idUsuario)).toList();
		}

		@Override
		public List<RotaSalva> listarTodas() {
			return List.copyOf(porId.values());
		}

		@Override
		public void excluir(Long id) {
			porId.remove(id);
		}
	}

	static final class Agendamentos implements RotaAgendadaRepository {

		private final List<RotaAgendada> todos = new ArrayList<>();
		private long sequencia;

		@Override
		public RotaAgendada salvar(RotaAgendada a) {
			Long id = a.getId() != null ? a.getId() : ++sequencia;
			RotaAgendada salvo = RotaAgendada.reconstruir(id, a.getIdRotaSalva(), a.getHorarioPartidaMinutos(),
					a.getAntecedenciaMinutos(), a.isAtiva(), a.getDiasSemana(), a.getCriadoEm());
			todos.removeIf(x -> x.getId().equals(id));
			todos.add(salvo);
			return salvo;
		}

		@Override
		public Optional<RotaAgendada> buscarPorId(Long id) {
			return todos.stream().filter(a -> a.getId().equals(id)).findFirst();
		}

		@Override
		public List<RotaAgendada> listarPorRotaSalva(Long idRotaSalva) {
			return todos.stream().filter(a -> a.getIdRotaSalva().equals(idRotaSalva)).toList();
		}

		@Override
		public List<RotaAgendada> listarPorUsuario(UUID idUsuario) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<RotaAgendada> listarAtivasPorDia(DiaSemana diaSemana) {
			return todos.stream().filter(a -> a.isAtiva() && a.executaNo(diaSemana)).toList();
		}

		@Override
		public void excluir(Long id) {
			todos.removeIf(a -> a.getId().equals(id));
		}
	}

}

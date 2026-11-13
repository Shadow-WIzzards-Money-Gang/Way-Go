package com.bytestorm.waygo.application.service;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bytestorm.waygo.application.cache.GrafoRedeCache;
import com.bytestorm.waygo.application.cache.StatusLinhasCache;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.entity.LinhaStatus;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.interfaces.EstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaEstacaoRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaStatusRepository;
import com.bytestorm.waygo.domain.interfaces.LinhaTransporteRepository;
import com.bytestorm.waygo.domain.valueobject.Pagina;

/** Repositórios da rede em memória, para testar os services sem banco. */
final class RedeEmMemoria {

	final Linhas linhas = new Linhas();
	final Estacoes estacoes = new Estacoes();
	final LinhaEstacoes linhaEstacoes = new LinhaEstacoes();
	final Status status = new Status();

	StatusLinhasCache statusCache(Clock clock) {
		return new StatusLinhasCache(linhas, status, clock, Duration.ofMinutes(30));
	}

	GrafoRedeCache grafoCache(Clock clock) {
		return new GrafoRedeCache(linhas, estacoes, linhaEstacoes, null, null, clock, Duration.ofHours(6));
	}

	LinhaService linhaService() {
		return new LinhaService(linhas, linhaEstacoes, estacoes, status);
	}

	static final class Linhas implements LinhaTransporteRepository {

		private final Map<Long, LinhaTransporte> porId = new LinkedHashMap<>();
		private long sequencia;

		@Override
		public List<LinhaTransporte> listarTodas() {
			return List.copyOf(porId.values());
		}

		@Override
		public Optional<LinhaTransporte> buscarPorId(Long id) {
			// Cópia, como o banco: alterar o objeto devolvido não muda o "salvo".
			return Optional.ofNullable(porId.get(id)).map(Linhas::copia);
		}

		@Override
		public List<LinhaTransporte> buscarPorIds(Collection<Long> ids) {
			return ids.stream().map(porId::get).filter(l -> l != null).toList();
		}

		@Override
		public boolean existePorId(Long id) {
			return porId.containsKey(id);
		}

		@Override
		public boolean existePorCodigo(String codigo) {
			return porId.values().stream().anyMatch(l -> l.possuiCodigo(codigo));
		}

		@Override
		public LinhaTransporte salvar(LinhaTransporte linha) {
			Long id = linha.getId() != null ? linha.getId() : ++sequencia;
			LinhaTransporte salva = LinhaTransporte.reconstruir(id, linha.getNome(), linha.getCodigo(), linha.getCor(),
					linha.getOperadora(), linha.getTipoTransporte(), linha.isAtiva());
			porId.put(id, salva);
			return copia(salva);
		}

		private static LinhaTransporte copia(LinhaTransporte l) {
			return LinhaTransporte.reconstruir(l.getId(), l.getNome(), l.getCodigo(), l.getCor(), l.getOperadora(),
					l.getTipoTransporte(), l.isAtiva());
		}

	}

	static final class Estacoes implements EstacaoRepository {

		private final Map<Long, Estacao> porId = new LinkedHashMap<>();
		private long sequencia;

		@Override
		public List<Estacao> listarTodas() {
			return List.copyOf(porId.values());
		}

		@Override
		public Pagina<Estacao> listarPaginado(String busca, boolean somenteIntegracoes, int pagina, int tamanho) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<Estacao> buscarPorId(Long id) {
			return Optional.ofNullable(porId.get(id)).map(Estacoes::copia);
		}

		@Override
		public List<Estacao> buscarPorIds(Collection<Long> ids) {
			return ids.stream().map(porId::get).filter(e -> e != null).toList();
		}

		@Override
		public boolean existePorId(Long id) {
			return porId.containsKey(id);
		}

		@Override
		public Estacao salvar(Estacao estacao) {
			Long id = estacao.getId() != null ? estacao.getId() : ++sequencia;
			Estacao salva = Estacao.reconstruir(id, estacao.getNome(), estacao.getCoordenada(), estacao.isAtiva());
			porId.put(id, salva);
			return copia(salva);
		}

		private static Estacao copia(Estacao e) {
			return Estacao.reconstruir(e.getId(), e.getNome(), e.getCoordenada(), e.isAtiva());
		}

	}

	static final class LinhaEstacoes implements LinhaEstacaoRepository {

		private final Map<Long, LinhaEstacao> porId = new LinkedHashMap<>();
		private long sequencia;

		void adicionar(Long linhaId, Long estacaoId, int posicao) {
			long id = ++sequencia;
			porId.put(id, LinhaEstacao.reconstruir(id, linhaId, estacaoId, posicao, true));
		}

		@Override
		public List<LinhaEstacao> listarTodas() {
			return List.copyOf(porId.values());
		}

		@Override
		public List<LinhaEstacao> listarPorLinhaOrdenadoPorSequencia(Long linhaId) {
			return porId.values().stream()
					.filter(le -> le.pertenceALinha(linhaId))
					.sorted(Comparator.comparingInt(LinhaEstacao::getSequencia))
					.map(le -> LinhaEstacao.reconstruir(le.getId(), le.getLinhaId(), le.getEstacaoId(),
							le.getSequencia(), le.isAtiva()))
					.toList();
		}

		@Override
		public List<LinhaEstacao> listarPorEstacao(Long estacaoId) {
			return porId.values().stream().filter(le -> le.getEstacaoId().equals(estacaoId)).toList();
		}

		@Override
		public List<LinhaEstacao> buscarPorIds(Collection<Long> ids) {
			return ids.stream().map(porId::get).filter(le -> le != null).toList();
		}

		@Override
		public void salvarSequencias(List<LinhaEstacao> linhaEstacoes) {
			linhaEstacoes.forEach(le -> porId.put(le.getId(), le));
		}

	}

	static final class Status implements LinhaStatusRepository {

		final List<LinhaStatus> historico = new ArrayList<>();
		private long sequencia;

		@Override
		public Optional<LinhaStatus> buscarAtualPorLinha(Long linhaId) {
			return historico.stream()
					.filter(s -> s.getLinhaId().equals(linhaId))
					.reduce((primeiro, segundo) -> segundo);
		}

		@Override
		public List<LinhaStatus> buscarAtuaisDeTodasAsLinhas() {
			return historico.stream()
					.map(LinhaStatus::getLinhaId)
					.distinct()
					.map(id -> buscarAtualPorLinha(id).orElseThrow())
					.toList();
		}

		@Override
		public LinhaStatus salvar(LinhaStatus s) {
			LinhaStatus salvo = LinhaStatus.reconstruir(++sequencia, s.getLinhaId(), s.getStatus(), s.getDescricao(),
					s.getOrigem(), s.getAtualizadoEm());
			historico.add(salvo);
			return salvo;
		}

	}

}

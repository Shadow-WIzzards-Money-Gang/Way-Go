package com.bytestorm.waygo.domain.valueobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.bytestorm.waygo.domain.entity.Baldeacao;
import com.bytestorm.waygo.domain.entity.ConexaoEstacao;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.enums.TipoConexao;

public final class GrafoRede {

	private final Map<Long, NoRede> nos;
	private final Map<Long, List<ArestaRede>> arestasPorOrigem;
	private final Map<Long, List<NoRede>> nosPorEstacao;
	private final Map<Long, List<NoRede>> nosPorLinha;
	private final Map<Long, LinhaTransporte> linhas;
	private final Map<Long, Estacao> estacoes;

	private GrafoRede(Map<Long, NoRede> nos, Map<Long, List<ArestaRede>> arestasPorOrigem,
			Map<Long, LinhaTransporte> linhas, Map<Long, Estacao> estacoes) {
		this.nos = Map.copyOf(nos);
		this.arestasPorOrigem = arestasPorOrigem.entrySet().stream()
				.collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, e -> List.copyOf(e.getValue())));
		this.nosPorEstacao = nos.values().stream()
				.collect(Collectors.groupingBy(NoRede::estacaoId, Collectors.toUnmodifiableList()));
		this.nosPorLinha = nos.values().stream()
				.sorted(Comparator.comparingInt(NoRede::sequencia))
				.collect(Collectors.groupingBy(NoRede::linhaId, Collectors.toUnmodifiableList()));
		this.linhas = Map.copyOf(linhas);
		this.estacoes = Map.copyOf(estacoes);
	}

	public static GrafoRede montar(Collection<LinhaTransporte> linhas, Collection<Estacao> estacoes,
			Collection<LinhaEstacao> linhasEstacoes, Collection<ConexaoEstacao> conexoes,
			Collection<Baldeacao> baldeacoes) {
		Map<Long, LinhaTransporte> linhasAtivas = linhas.stream()
				.filter(LinhaTransporte::isAtiva)
				.collect(Collectors.toMap(LinhaTransporte::getId, Function.identity()));
		Map<Long, Estacao> estacoesAtivas = estacoes.stream()
				.filter(Estacao::isAtiva)
				.collect(Collectors.toMap(Estacao::getId, Function.identity()));

		Map<Long, NoRede> nos = new HashMap<>();
		for (LinhaEstacao le : linhasEstacoes) {
			if (le.isAtiva() && linhasAtivas.containsKey(le.getLinhaId())
					&& estacoesAtivas.containsKey(le.getEstacaoId())) {
				nos.put(le.getId(), new NoRede(le.getId(), le.getLinhaId(), le.getEstacaoId(), le.getSequencia()));
			}
		}

		Map<Long, List<ArestaRede>> arestas = new HashMap<>();
		for (ConexaoEstacao conexao : conexoes) {
			if (conexao.isAtiva() && conexao.getTempoViagemSegundos() != null) {
				adicionar(arestas, nos, new ArestaRede(conexao.getLinhaEstacaoOrigemId(),
						conexao.getLinhaEstacaoDestinoId(), TipoConexao.CONEXAO, conexao.getTempoViagemSegundos(),
						valorOuZero(conexao.getDistanciaMetros())));
			}
		}
		for (Baldeacao baldeacao : baldeacoes) {
			if (baldeacao.isAtiva() && baldeacao.getTempoBaldeacaoSegundos() != null) {
				adicionar(arestas, nos, new ArestaRede(baldeacao.getLinhaEstacaoOrigemId(),
						baldeacao.getLinhaEstacaoDestinoId(), TipoConexao.BALDEACAO,
						baldeacao.getTempoBaldeacaoSegundos(), 0));
			}
		}

		Map<Long, LinhaTransporte> linhasComNos = new HashMap<>();
		Map<Long, Estacao> estacoesComNos = new HashMap<>();
		for (NoRede no : nos.values()) {
			linhasComNos.put(no.linhaId(), linhasAtivas.get(no.linhaId()));
			estacoesComNos.put(no.estacaoId(), estacoesAtivas.get(no.estacaoId()));
		}
		return new GrafoRede(nos, arestas, linhasComNos, estacoesComNos);
	}

	private static void adicionar(Map<Long, List<ArestaRede>> arestas, Map<Long, NoRede> nos, ArestaRede aresta) {
		if (nos.containsKey(aresta.origem()) && nos.containsKey(aresta.destino())) {
			arestas.computeIfAbsent(aresta.origem(), id -> new ArrayList<>()).add(aresta);
		}
	}

	private static int valorOuZero(Integer valor) {
		return valor == null ? 0 : valor;
	}

	public NoRede no(Long linhaEstacaoId) {
		return nos.get(linhaEstacaoId);
	}

	public List<ArestaRede> arestasDe(Long linhaEstacaoId) {
		return arestasPorOrigem.getOrDefault(linhaEstacaoId, List.of());
	}

	public List<NoRede> nosDaEstacao(Long estacaoId) {
		return nosPorEstacao.getOrDefault(estacaoId, List.of());
	}

	/** Nós da linha ordenados por sequência. */
	public List<NoRede> nosDaLinha(Long linhaId) {
		return nosPorLinha.getOrDefault(linhaId, List.of());
	}

	public Optional<LinhaTransporte> linha(Long linhaId) {
		return Optional.ofNullable(linhas.get(linhaId));
	}

	public Optional<Estacao> estacao(Long estacaoId) {
		return Optional.ofNullable(estacoes.get(estacaoId));
	}

	public int quantidadeNos() {
		return nos.size();
	}

}

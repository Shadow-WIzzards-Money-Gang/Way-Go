package com.bytestorm.waygo.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.domain.entity.Baldeacao;
import com.bytestorm.waygo.domain.entity.ConexaoEstacao;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;
import com.bytestorm.waygo.domain.enums.CriterioRota;
import com.bytestorm.waygo.domain.enums.StatusOperacional;
import com.bytestorm.waygo.domain.enums.TipoTransporte;
import com.bytestorm.waygo.domain.valueobject.CaminhoRota;
import com.bytestorm.waygo.domain.valueobject.GrafoRede;
import com.bytestorm.waygo.domain.valueobject.NoRede;
import com.bytestorm.waygo.domain.valueobject.ParametrosRota;

/**
 * Rede de teste: estações A (1) e B (2) ligadas por duas linhas paralelas.
 * Linha 10 (rápida): A → B em 300 s. Linha 20 (lenta): A → B em 900 s.
 * A estação C (3) só é alcançável pela linha 10.
 */
class CalculadoraRotaTest {

	private static final Long A = 1L;
	private static final Long B = 2L;
	private static final Long C = 3L;
	private static final Long RAPIDA = 10L;
	private static final Long LENTA = 20L;

	private final CalculadoraRota calculadora = new CalculadoraRota();
	private final GrafoRede grafo = montarGrafo();

	private static GrafoRede montarGrafo() {
		List<LinhaTransporte> linhas = List.of(
				LinhaTransporte.reconstruir(RAPIDA, "Linha 10", "10", "#000000", "Op", TipoTransporte.METRO, true),
				LinhaTransporte.reconstruir(LENTA, "Linha 20", "20", "#FFFFFF", "Op", TipoTransporte.METRO, true));
		List<Estacao> estacoes = List.of(
				Estacao.reconstruir(A, "A", null, true),
				Estacao.reconstruir(B, "B", null, true),
				Estacao.reconstruir(C, "C", null, true));
		// Nós (LinhaEstacao): 101 = A/10, 102 = B/10, 103 = C/10, 201 = A/20, 202 = B/20
		List<LinhaEstacao> nos = List.of(
				LinhaEstacao.reconstruir(101L, RAPIDA, A, 1, true),
				LinhaEstacao.reconstruir(102L, RAPIDA, B, 2, true),
				LinhaEstacao.reconstruir(103L, RAPIDA, C, 3, true),
				LinhaEstacao.reconstruir(201L, LENTA, A, 1, true),
				LinhaEstacao.reconstruir(202L, LENTA, B, 2, true));
		List<ConexaoEstacao> conexoes = new ArrayList<>();
		conexoes.add(ConexaoEstacao.reconstruir(1L, 101L, 102L, 300, 1000, true));
		conexoes.add(ConexaoEstacao.reconstruir(2L, 102L, 103L, 300, 1000, true));
		conexoes.add(ConexaoEstacao.reconstruir(3L, 201L, 202L, 900, 1000, true));
		List<Baldeacao> baldeacoes = List.of();
		return GrafoRede.montar(linhas, estacoes, nos, conexoes, baldeacoes);
	}

	private static ParametrosRota parametros(Map<Long, StatusOperacional> status, boolean somenteNormais) {
		return new ParametrosRota(CriterioRota.MENOR_TEMPO, 1.5, 180, status, somenteNormais);
	}

	private static List<Long> linhasUsadas(CaminhoRota caminho) {
		return caminho.nos().stream().map(NoRede::linhaId).distinct().toList();
	}

	@Test
	void semAlternativaUsaLinhaComProblemaSeForMaisRapida() {
		var status = Map.of(RAPIDA, StatusOperacional.VELOCIDADE_REDUZIDA);

		Optional<CaminhoRota> caminho = calculadora.calcular(grafo, A, B, parametros(status, false));

		assertThat(caminho).isPresent();
		assertThat(linhasUsadas(caminho.get())).containsExactly(RAPIDA);
	}

	@Test
	void alternativaDesviaPelaLinhaNormal() {
		var status = Map.of(RAPIDA, StatusOperacional.VELOCIDADE_REDUZIDA);

		Optional<CaminhoRota> caminho = calculadora.calcular(grafo, A, B, parametros(status, true));

		assertThat(caminho).isPresent();
		assertThat(linhasUsadas(caminho.get())).containsExactly(LENTA);
	}

	@Test
	void alternativaTrataStatusDesconhecidoComoNormal() {
		var status = Map.of(RAPIDA, StatusOperacional.DESCONHECIDO);

		Optional<CaminhoRota> caminho = calculadora.calcular(grafo, A, B, parametros(status, true));

		assertThat(caminho).isPresent();
		assertThat(linhasUsadas(caminho.get())).containsExactly(RAPIDA);
	}

	@Test
	void alternativaSemCaminhoNormalNaoEncontraRota() {
		var status = Map.of(RAPIDA, StatusOperacional.OPERACAO_PARCIAL);

		assertThat(calculadora.calcular(grafo, A, C, parametros(status, false))).isPresent();
		assertThat(calculadora.calcular(grafo, A, C, parametros(status, true))).isEmpty();
	}

	@Test
	void linhaDisponivelRespeitaOModo() {
		var status = Map.of(
				RAPIDA, StatusOperacional.VELOCIDADE_REDUZIDA,
				LENTA, StatusOperacional.PARALISADA);
		ParametrosRota normal = parametros(status, false);
		ParametrosRota alternativa = parametros(status, true);

		assertThat(normal.linhaDisponivel(RAPIDA)).isTrue();
		assertThat(normal.linhaDisponivel(LENTA)).isFalse();
		assertThat(alternativa.linhaDisponivel(RAPIDA)).isFalse();
		assertThat(alternativa.linhaDisponivel(LENTA)).isFalse();
		// Linha sem status registrado conta como normal nos dois modos.
		assertThat(alternativa.linhaDisponivel(99L)).isTrue();
	}

}

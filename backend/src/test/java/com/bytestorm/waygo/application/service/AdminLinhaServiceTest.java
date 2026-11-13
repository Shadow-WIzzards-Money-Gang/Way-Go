package com.bytestorm.waygo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.application.cache.StatusLinhasCache;
import com.bytestorm.waygo.application.dto.request.LinhaAtualizacaoRequest;
import com.bytestorm.waygo.application.dto.request.LinhaRequest;
import com.bytestorm.waygo.application.dto.request.OrdemEstacoesRequest;
import com.bytestorm.waygo.application.dto.request.StatusLinhaRequest;
import com.bytestorm.waygo.application.dto.response.EstacaoDaLinhaResponse;
import com.bytestorm.waygo.application.dto.response.LinhaResumoResponse;
import com.bytestorm.waygo.application.dto.response.LinhaStatusResponse;
import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.enums.OrigemStatus;
import com.bytestorm.waygo.domain.enums.StatusOperacional;
import com.bytestorm.waygo.domain.enums.TipoTransporte;
import com.bytestorm.waygo.domain.exception.ConflitoException;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;

class AdminLinhaServiceTest {

	private static final Instant AGORA = Instant.parse("2026-11-13T12:00:00Z");
	private final Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);

	private final RedeEmMemoria rede = new RedeEmMemoria();
	private final StatusLinhasCache statusCache = rede.statusCache(clock);
	private final AdminLinhaService service = new AdminLinhaService(rede.linhas, rede.linhaEstacoes, rede.status,
			rede.linhaService(), rede.grafoCache(clock), statusCache, clock);

	private LinhaResumoResponse criarLinha(String codigo) {
		return service.criar(new LinhaRequest("Linha " + codigo, codigo, "#123456", "Metrô", TipoTransporte.METRO));
	}

	@Test
	void criaLinhaAtivaComOsDadosInformados() {
		LinhaResumoResponse linha = criarLinha("6");

		assertThat(linha.id()).isNotNull();
		assertThat(linha.codigo()).isEqualTo("6");
		assertThat(linha.ativa()).isTrue();
		assertThat(rede.linhas.existePorId(linha.id())).isTrue();
	}

	@Test
	void rejeitaCodigoRepetidoComConflito() {
		criarLinha("6");

		assertThatThrownBy(() -> criarLinha(" 6 ")).isInstanceOf(ConflitoException.class);
	}

	@Test
	void dadosInvalidosDoDominioViram400() {
		assertThatThrownBy(() -> service.criar(new LinhaRequest("L", "9", "red1234", "Op", TipoTransporte.METRO)))
				.isInstanceOf(RequisicaoInvalidaException.class)
				.hasMessageContaining("#RRGGBB");
	}

	@Test
	void atualizacaoParcialMantemOsCamposOmitidos() {
		Long id = criarLinha("6").id();

		LinhaResumoResponse atualizada = service.atualizar(id, new LinhaAtualizacaoRequest("Laranja", null, null, null));

		assertThat(atualizada.nome()).isEqualTo("Laranja");
		assertThat(atualizada.cor()).isEqualTo("#123456");
		assertThat(atualizada.operadora()).isEqualTo("Metrô");
		assertThat(atualizada.ativa()).isTrue();
	}

	@Test
	void desativarEReativar() {
		Long id = criarLinha("6").id();

		service.desativar(id);
		assertThat(rede.linhas.buscarPorId(id).orElseThrow().isAtiva()).isFalse();

		service.atualizar(id, new LinhaAtualizacaoRequest(null, null, null, true));
		assertThat(rede.linhas.buscarPorId(id).orElseThrow().isAtiva()).isTrue();
	}

	@Test
	void linhaInexistenteDa404() {
		assertThatThrownBy(() -> service.desativar(99L)).isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void reordenaAsEstacoesDaLinha() {
		Long linha = criarLinha("6").id();
		Long a = rede.estacoes.salvar(Estacao.criar("A", null)).getId();
		Long b = rede.estacoes.salvar(Estacao.criar("B", null)).getId();
		Long c = rede.estacoes.salvar(Estacao.criar("C", null)).getId();
		rede.linhaEstacoes.adicionar(linha, a, 1);
		rede.linhaEstacoes.adicionar(linha, b, 2);
		rede.linhaEstacoes.adicionar(linha, c, 3);

		List<EstacaoDaLinhaResponse> ordem = service.reordenarEstacoes(linha, new OrdemEstacoesRequest(List.of(c, a, b)));

		assertThat(ordem).extracting(EstacaoDaLinhaResponse::nome).containsExactly("C", "A", "B");
		assertThat(ordem).extracting(EstacaoDaLinhaResponse::sequencia).containsExactly(1, 2, 3);
	}

	@Test
	void reordenarExigeTodasAsEstacoesSemRepetir() {
		Long linha = criarLinha("6").id();
		Long a = rede.estacoes.salvar(Estacao.criar("A", null)).getId();
		Long b = rede.estacoes.salvar(Estacao.criar("B", null)).getId();
		rede.linhaEstacoes.adicionar(linha, a, 1);
		rede.linhaEstacoes.adicionar(linha, b, 2);

		assertThatThrownBy(() -> service.reordenarEstacoes(linha, new OrdemEstacoesRequest(List.of(a))))
				.isInstanceOf(RequisicaoInvalidaException.class);
		assertThatThrownBy(() -> service.reordenarEstacoes(linha, new OrdemEstacoesRequest(List.of(a, a))))
				.isInstanceOf(RequisicaoInvalidaException.class);
		assertThatThrownBy(() -> service.reordenarEstacoes(linha, new OrdemEstacoesRequest(List.of(a, 999L))))
				.isInstanceOf(RequisicaoInvalidaException.class);
	}

	@Test
	void statusManualFicaComOrigemAdministradorEAtualizaOCache() {
		Long id = criarLinha("6").id();

		LinhaStatusResponse status = service.alterarStatus(id,
				new StatusLinhaRequest(StatusOperacional.PARALISADA, "Falha de energia"));

		assertThat(status.origem()).isEqualTo(OrigemStatus.ADMINISTRADOR);
		assertThat(status.status()).isEqualTo(StatusOperacional.PARALISADA);
		assertThat(status.atualizadoEm()).isEqualTo(LocalDateTime.ofInstant(AGORA, ZoneOffset.UTC));
		assertThat(statusCache.statusAtuais()).containsEntry(id, StatusOperacional.PARALISADA);
	}

}

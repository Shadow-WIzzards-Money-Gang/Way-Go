package com.bytestorm.waygo.application.dto.response;

import com.bytestorm.waygo.domain.entity.Estacao;
import com.bytestorm.waygo.domain.entity.LinhaEstacao;
import com.bytestorm.waygo.domain.entity.LinhaTransporte;

public record PontoConexaoResponse(
		Long estacaoId,
		String estacaoNome,
		Long linhaId,
		String linhaNome,
		String linhaCodigo,
		String linhaCor,
		int sequencia) {

	public static PontoConexaoResponse de(LinhaEstacao linhaEstacao, Estacao estacao, LinhaTransporte linha) {
		return new PontoConexaoResponse(estacao.getId(), estacao.getNome(), linha.getId(), linha.getNome(),
				linha.getCodigo(), linha.getCor(), linhaEstacao.getSequencia());
	}

}

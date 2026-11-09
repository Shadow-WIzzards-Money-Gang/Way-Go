package com.bytestorm.waygo.application.dto.response;

import com.bytestorm.waygo.domain.entity.Baldeacao;
import com.bytestorm.waygo.domain.entity.ConexaoEstacao;
import com.bytestorm.waygo.domain.enums.TipoConexao;

public record ConexaoResponse(
		TipoConexao tipo,
		PontoConexaoResponse origem,
		PontoConexaoResponse destino,
		Integer tempoSegundos,
		Integer distanciaMetros,
		boolean ativa) {

	public static ConexaoResponse de(ConexaoEstacao conexao, PontoConexaoResponse origem,
			PontoConexaoResponse destino) {
		return new ConexaoResponse(TipoConexao.CONEXAO, origem, destino, conexao.getTempoViagemSegundos(),
				conexao.getDistanciaMetros(), conexao.isAtiva());
	}

	public static ConexaoResponse de(Baldeacao baldeacao, PontoConexaoResponse origem, PontoConexaoResponse destino) {
		return new ConexaoResponse(TipoConexao.BALDEACAO, origem, destino, baldeacao.getTempoBaldeacaoSegundos(),
				null, baldeacao.isAtiva());
	}

}

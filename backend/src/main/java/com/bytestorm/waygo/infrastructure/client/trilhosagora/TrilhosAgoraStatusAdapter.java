package com.bytestorm.waygo.infrastructure.client.trilhosagora;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Component;

import com.bytestorm.waygo.domain.enums.StatusOperacional;
import com.bytestorm.waygo.domain.interfaces.FonteStatusLinhas;
import com.bytestorm.waygo.domain.valueobject.StatusLinhaExterno;
import com.bytestorm.waygo.infrastructure.client.trilhosagora.dto.LinhaTrilhosAgora;
import com.bytestorm.waygo.infrastructure.client.trilhosagora.dto.TrilhosAgoraStatusResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrilhosAgoraStatusAdapter implements FonteStatusLinhas {

	static final int LINHA_EXPRESSO_AEROPORTO = 100;
	static final String CODIGO_EXPRESSO_AEROPORTO = "expresso-aeroporto";

	// Limite em bytes da coluna LINHA_STATUS.DESCRICAO (VARCHAR2(255))
	private static final int TAMANHO_MAXIMO_DESCRICAO = 255;

	private final TrilhosAgoraClient client;

	@Override
	public List<StatusLinhaExterno> buscarStatusAtuais() {
		TrilhosAgoraStatusResponse resposta = client.buscarStatus();
		if (resposta.stale()) {
			log.warn("Trilhos Agora retornou dados repetidos (stale) coletados em {}", resposta.fetchedAt());
		}
		if (resposta.lines() == null) {
			return List.of();
		}
		return resposta.lines().stream()
				.map(this::paraDominio)
				.toList();
	}

	private StatusLinhaExterno paraDominio(LinhaTrilhosAgora linha) {
		StatusOperacional status = StatusOperacional.deTexto(linha.status());
		String descricao = linha.descricao();
		if (status == StatusOperacional.DESCONHECIDO) {
			log.warn("Status desconhecido recebido da Trilhos Agora para a linha {}: '{}'", linha.linha(),
					linha.status());
			if (descricao == null || descricao.isBlank()) {
				descricao = linha.status();
			}
		}
		return new StatusLinhaExterno(codigoLinha(linha.linha()), status, truncar(descricao));
	}

	static String codigoLinha(int linha) {
		return linha == LINHA_EXPRESSO_AEROPORTO ? CODIGO_EXPRESSO_AEROPORTO : String.valueOf(linha);
	}

	static String truncar(String texto) {
		if (texto == null || texto.isBlank()) {
			return null;
		}
		String resultado = texto.trim();
		while (resultado.getBytes(StandardCharsets.UTF_8).length > TAMANHO_MAXIMO_DESCRICAO) {
			resultado = resultado.substring(0, resultado.length() - 1);
		}
		return resultado;
	}

}

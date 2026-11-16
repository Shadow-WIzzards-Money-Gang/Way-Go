package com.bytestorm.waygo.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bytestorm.waygo.domain.entity.DispositivoUsuario;
import com.bytestorm.waygo.domain.entity.Notificacao;
import com.bytestorm.waygo.domain.enums.TipoNotificacao;
import com.bytestorm.waygo.domain.interfaces.DispositivoUsuarioRepository;
import com.bytestorm.waygo.domain.interfaces.EnviadorPush;
import com.bytestorm.waygo.domain.interfaces.NotificacaoRepository;
import com.bytestorm.waygo.domain.valueobject.ResultadoEnvioPush;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvioNotificacaoService {

	static final int LOTE = 100;
	static final String SEM_APARELHO = "Nenhum aparelho ativo para receber";

	private final NotificacaoRepository notificacaoRepository;
	private final DispositivoUsuarioRepository dispositivoRepository;
	private final EnviadorPush enviadorPush;
	private final Clock clock;

	public int processarFila() {
		LocalDateTime agora = LocalDateTime.now(clock);
		int enviadas = 0;
		for (Notificacao notificacao : notificacaoRepository.listarProntasParaEnvio(agora, LOTE)) {
			if (processar(notificacao, agora)) {
				enviadas++;
			}
		}
		return enviadas;
	}

	private boolean processar(Notificacao notificacao, LocalDateTime agora) {
		if (notificacao.expirarSeVencida(agora)) {
			notificacaoRepository.salvar(notificacao);
			log.info("Notificação {} expirou sem ser entregue após {} tentativa(s)", notificacao.getId(),
					notificacao.getTentativas());
			return false;
		}

		List<DispositivoUsuario> aparelhos = dispositivoRepository.listarAtivosDoUsuario(notificacao.getIdUsuario())
				.stream()
				.filter(d -> notificacao.getTipo() != TipoNotificacao.STATUS_LINHA || d.isAlertasStatus())
				.toList();

		boolean entregue = false;
		String erro = SEM_APARELHO;
		for (DispositivoUsuario aparelho : aparelhos) {
			ResultadoEnvioPush resultado = enviadorPush.enviar(aparelho.getTokenFcm(), notificacao.getTitulo(),
					notificacao.getCorpo(), dadosDo(notificacao));
			switch (resultado.tipo()) {
				case SUCESSO -> entregue = true;
				case TOKEN_INVALIDO -> {
					aparelho.desativar(agora);
					dispositivoRepository.salvar(aparelho);
					erro = resultado.mensagem();
					log.info("Aparelho {} desativado: token FCM inválido ({})", aparelho.getId(), resultado.mensagem());
				}
				case FALHA_TEMPORARIA -> erro = resultado.mensagem();
			}
		}

		if (entregue) {
			notificacao.marcarEnviada(agora);
		} else {
			notificacao.registrarFalha(erro, agora);
			log.debug("Notificação {} não entregue (tentativa {}): {}", notificacao.getId(),
					notificacao.getTentativas(), erro);
		}
		notificacaoRepository.salvar(notificacao);
		return entregue;
	}

	private static Map<String, String> dadosDo(Notificacao notificacao) {
		Map<String, String> dados = new LinkedHashMap<>(notificacao.getDados());
		dados.put("idNotificacao", String.valueOf(notificacao.getId()));
		dados.putIfAbsent("tipo", notificacao.getTipo().name());
		return dados;
	}

}

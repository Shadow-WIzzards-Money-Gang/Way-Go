package com.bytestorm.waygo.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.domain.enums.StatusNotificacao;
import com.bytestorm.waygo.domain.enums.TipoNotificacao;

class NotificacaoTest {

	private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 16, 7, 0);

	private Notificacao nova() {
		return Notificacao.criar(UUID.randomUUID(), TipoNotificacao.LEMBRETE_AGENDAMENTO, "agendamento:1:2026-11-16",
				"Casa → Trabalho: tudo certo", "Saída às 07:10.", Map.of("alternativa", "false"),
				AGORA.plusMinutes(10), AGORA);
	}

	@Test
	void nasceProntaParaEnvioImediato() {
		Notificacao notificacao = nova();

		assertThat(notificacao.getStatus()).isEqualTo(StatusNotificacao.PENDENTE);
		assertThat(notificacao.getProximaTentativaEm()).isEqualTo(AGORA);
		assertThat(notificacao.getTentativas()).isZero();
	}

	@Test
	void falhasReagendamComEsperaCrescenteAteOTeto() {
		Notificacao notificacao = nova();
		LocalDateTime momento = AGORA;
		long[] esperasEmSegundos = { 30, 60, 120, 300, 600, 900, 900 };

		for (long espera : esperasEmSegundos) {
			notificacao.registrarFalha("FCM fora do ar", momento);
			assertThat(notificacao.getProximaTentativaEm()).isEqualTo(momento.plusSeconds(espera));
			momento = notificacao.getProximaTentativaEm();
		}
		assertThat(notificacao.getTentativas()).isEqualTo(esperasEmSegundos.length);
		assertThat(notificacao.getUltimoErro()).isEqualTo("FCM fora do ar");
		assertThat(notificacao.estaPendente()).isTrue();
	}

	@Test
	void marcarEnviadaContaATentativaELimpaOErro() {
		Notificacao notificacao = nova();
		notificacao.registrarFalha("erro", AGORA);

		notificacao.marcarEnviada(AGORA.plusMinutes(1));

		assertThat(notificacao.getStatus()).isEqualTo(StatusNotificacao.ENVIADA);
		assertThat(notificacao.getEnviadaEm()).isEqualTo(AGORA.plusMinutes(1));
		assertThat(notificacao.getTentativas()).isEqualTo(2);
		assertThat(notificacao.getUltimoErro()).isNull();
		assertThatThrownBy(() -> notificacao.registrarFalha("x", AGORA)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void expiraSoDepoisDoPrazo() {
		Notificacao notificacao = nova();

		assertThat(notificacao.expirarSeVencida(AGORA.plusMinutes(9))).isFalse();
		assertThat(notificacao.expirarSeVencida(AGORA.plusMinutes(10))).isTrue();
		assertThat(notificacao.getStatus()).isEqualTo(StatusNotificacao.EXPIRADA);
	}

	@Test
	void cortaTextosLongosEmVezDeFalhar() {
		Notificacao notificacao = Notificacao.criar(UUID.randomUUID(), TipoNotificacao.STATUS_LINHA, "status:1",
				"t".repeat(300), "c".repeat(2000), null, AGORA.plusHours(1), AGORA);

		assertThat(notificacao.getTitulo()).hasSize(150).endsWith("…");
		assertThat(notificacao.getCorpo()).hasSize(1000);
		assertThat(notificacao.getDados()).isEmpty();
	}

}

package com.bytestorm.waygo.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.domain.enums.OrigemStatus;
import com.bytestorm.waygo.domain.enums.StatusOperacional;

class LinhaStatusTest {

	private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 11, 13, 48);

	@Test
	void fabricasNomeadasFixamAOrigem() {
		LinhaStatus api = LinhaStatus.registrarPelaApi(11L, StatusOperacional.VELOCIDADE_REDUZIDA, "Entre Brás e "
				+ "Guaianases", AGORA);
		LinhaStatus admin = LinhaStatus.registrarPeloAdministrador(11L, StatusOperacional.NORMAL, null, AGORA);

		assertThat(api.getId()).isNull();
		assertThat(api.getOrigem()).isEqualTo(OrigemStatus.API);
		assertThat(admin.getOrigem()).isEqualTo(OrigemStatus.ADMINISTRADOR);
	}

	@Test
	void reconstruirPreservaTodosOsCampos() {
		LinhaStatus status = LinhaStatus.reconstruir(1L, 10L, StatusOperacional.PARALISADA, "Falha", OrigemStatus.API,
				AGORA);

		assertThat(status.getId()).isEqualTo(1L);
		assertThat(status.getLinhaId()).isEqualTo(10L);
		assertThat(status.getStatus()).isEqualTo(StatusOperacional.PARALISADA);
		assertThat(status.getDescricao()).isEqualTo("Falha");
		assertThat(status.getAtualizadoEm()).isEqualTo(AGORA);
	}

	@Test
	void delegaRegrasDeCirculacaoAoStatus() {
		assertThat(LinhaStatus.registrarPelaApi(1L, StatusOperacional.PARALISADA, null, AGORA).bloqueiaCirculacao())
				.isTrue();
		assertThat(LinhaStatus.registrarPelaApi(1L, StatusOperacional.VELOCIDADE_REDUZIDA, null, AGORA).afetaTempo())
				.isTrue();
		assertThat(LinhaStatus.registrarPelaApi(1L, StatusOperacional.NORMAL, null, AGORA).bloqueiaCirculacao())
				.isFalse();
	}

	@Test
	void detectaMudancaDeStatus() {
		LinhaStatus status = LinhaStatus.registrarPelaApi(1L, StatusOperacional.NORMAL, null, AGORA);

		assertThat(status.mudouEmRelacaoA(StatusOperacional.NORMAL)).isFalse();
		assertThat(status.mudouEmRelacaoA(StatusOperacional.PARALISADA)).isTrue();
		assertThat(status.mudouEmRelacaoA(null)).isTrue();
	}

	@Test
	void validaCamposObrigatoriosEDescricao() {
		assertThatThrownBy(() -> LinhaStatus.registrarPelaApi(null, StatusOperacional.NORMAL, null, AGORA))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> LinhaStatus.registrarPelaApi(1L, StatusOperacional.NORMAL, "x".repeat(256), AGORA))
				.isInstanceOf(IllegalArgumentException.class);
		assertThat(LinhaStatus.registrarPelaApi(1L, StatusOperacional.NORMAL, "  ", AGORA).getDescricao()).isNull();
	}

}

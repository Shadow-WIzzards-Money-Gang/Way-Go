package com.bytestorm.waygo.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.bytestorm.waygo.domain.interfaces.SenhaHasher;

class TokenRecuperacaoSenhaTest {

	private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 16, 10, 0);

	private static final SenhaHasher HASHER = new SenhaHasher() {
		@Override
		public String gerarHash(String senha) {
			return "hash:" + senha;
		}

		@Override
		public boolean corresponde(String senha, String hash) {
			return hash.equals(gerarHash(senha));
		}
	};

	private final TokenRecuperacaoSenha token = TokenRecuperacaoSenha.criar(UUID.randomUUID(), "123456", HASHER,
			AGORA);

	@Test
	void criarGuardaSoOHashEExpiraEm15Minutos() {
		assertThat(token.getCodigoHash()).isEqualTo("hash:123456");
		assertThat(token.getExpiraEm()).isEqualTo(AGORA.plusMinutes(15));
		assertThat(token.getTentativas()).isZero();
		assertThat(token.getUsadoEm()).isNull();
	}

	@Test
	void confereSoOCodigoCerto() {
		assertThat(token.confere("123456", HASHER)).isTrue();
		assertThat(token.confere("654321", HASHER)).isFalse();
		assertThat(token.confere(null, HASHER)).isFalse();
	}

	@Test
	void deixaDeValerAoExpirar() {
		assertThat(token.estaValido(AGORA.plusMinutes(14))).isTrue();
		assertThat(token.estaValido(AGORA.plusMinutes(15))).isFalse();
	}

	@Test
	void deixaDeValerAposCincoTentativasErradas() {
		for (int i = 0; i < TokenRecuperacaoSenha.MAX_TENTATIVAS - 1; i++) {
			token.registrarTentativaFalha();
		}
		assertThat(token.estaValido(AGORA)).isTrue();

		token.registrarTentativaFalha();
		assertThat(token.estaValido(AGORA)).isFalse();
	}

	@Test
	void ehDeUsoUnico() {
		token.marcarUsado(AGORA);

		assertThat(token.estaValido(AGORA)).isFalse();
		assertThatThrownBy(() -> token.marcarUsado(AGORA)).isInstanceOf(IllegalStateException.class);
	}

	@Test
	void sabeSeFoiCriadoHaPouco() {
		assertThat(token.foiCriadoHaMenosDe(Duration.ofMinutes(1), AGORA.plusSeconds(59))).isTrue();
		assertThat(token.foiCriadoHaMenosDe(Duration.ofMinutes(1), AGORA.plusSeconds(60))).isFalse();
	}

	@Test
	void recusaTentativasNegativas() {
		assertThatThrownBy(() -> TokenRecuperacaoSenha.reconstruir(1L, UUID.randomUUID(), "hash", AGORA, -1, null,
				AGORA)).isInstanceOf(IllegalArgumentException.class);
	}

}

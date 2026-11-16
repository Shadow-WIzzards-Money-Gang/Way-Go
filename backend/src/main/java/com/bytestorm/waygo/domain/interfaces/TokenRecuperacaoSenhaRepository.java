package com.bytestorm.waygo.domain.interfaces;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.bytestorm.waygo.domain.entity.TokenRecuperacaoSenha;

public interface TokenRecuperacaoSenhaRepository {

	TokenRecuperacaoSenha salvar(TokenRecuperacaoSenha token);

	Optional<TokenRecuperacaoSenha> buscarMaisRecenteDoUsuario(UUID idUsuario);

	void invalidarAtivosDoUsuario(UUID idUsuario, LocalDateTime agora);

}

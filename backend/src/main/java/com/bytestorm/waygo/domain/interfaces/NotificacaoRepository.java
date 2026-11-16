package com.bytestorm.waygo.domain.interfaces;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.bytestorm.waygo.domain.entity.Notificacao;

public interface NotificacaoRepository {

	Optional<Notificacao> salvarSeNova(Notificacao notificacao);

	boolean existe(UUID idUsuario, String chave);

	List<Notificacao> listarProntasParaEnvio(LocalDateTime agora, int limite);

	Notificacao salvar(Notificacao notificacao);

}

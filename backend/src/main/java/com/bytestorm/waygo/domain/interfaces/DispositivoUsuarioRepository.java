package com.bytestorm.waygo.domain.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.bytestorm.waygo.domain.entity.DispositivoUsuario;

public interface DispositivoUsuarioRepository {

	DispositivoUsuario salvar(DispositivoUsuario dispositivo);

	Optional<DispositivoUsuario> buscarPorToken(String tokenFcm);

	List<DispositivoUsuario> listarAtivosDoUsuario(UUID idUsuario);

	Set<UUID> listarUsuariosComAlertasStatus();

	void excluirPorTokenEUsuario(String tokenFcm, UUID idUsuario);

}

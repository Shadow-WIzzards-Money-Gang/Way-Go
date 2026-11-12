package com.bytestorm.waygo.domain.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.bytestorm.waygo.domain.entity.RotaSalva;

public interface RotaSalvaRepository {

	RotaSalva salvar(RotaSalva rotaSalva);

	Optional<RotaSalva> buscarPorId(Long id);

	Optional<RotaSalva> buscarPorIdEUsuario(Long id, UUID idUsuario);

	List<RotaSalva> listarPorUsuario(UUID idUsuario);

	void excluir(Long id);

}

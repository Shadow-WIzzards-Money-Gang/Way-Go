package com.bytestorm.waygo.domain.interfaces;

import java.util.Optional;
import java.util.UUID;

import com.bytestorm.waygo.domain.entity.Usuario;

public interface UsuarioRepository {

	Usuario salvar(Usuario usuario);

	Optional<Usuario> buscarPorId(UUID id);

	Optional<Usuario> buscarPorEmail(String email);

	boolean existePorEmail(String email);

}

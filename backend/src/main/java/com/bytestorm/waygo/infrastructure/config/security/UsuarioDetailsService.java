package com.bytestorm.waygo.infrastructure.config.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bytestorm.waygo.domain.interfaces.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) {
		return usuarioRepository.buscarPorEmail(email)
				.map(UsuarioAutenticado::new)
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
	}

}

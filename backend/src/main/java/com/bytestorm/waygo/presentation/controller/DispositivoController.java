package com.bytestorm.waygo.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.request.DispositivoRequest;
import com.bytestorm.waygo.application.service.DispositivoService;
import com.bytestorm.waygo.infrastructure.config.security.UsuarioAutenticado;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/dispositivos")
public class DispositivoController {

	private final DispositivoService dispositivoService;

	public DispositivoController(DispositivoService dispositivoService) {
		this.dispositivoService = dispositivoService;
	}

	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> registrar(Authentication authentication,
			@Valid @RequestBody DispositivoRequest request) {
		dispositivoService.registrar(usuario(authentication).getUsuario().getId(), request);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> remover(Authentication authentication, @RequestParam String token) {
		dispositivoService.remover(usuario(authentication).getUsuario().getId(), token);
		return ResponseEntity.noContent().build();
	}

	private static UsuarioAutenticado usuario(Authentication authentication) {
		return (UsuarioAutenticado) authentication.getPrincipal();
	}

}

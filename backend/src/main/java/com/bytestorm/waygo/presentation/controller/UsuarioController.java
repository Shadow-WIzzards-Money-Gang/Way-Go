package com.bytestorm.waygo.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.response.PaginaResponse;
import com.bytestorm.waygo.application.dto.response.UsuarioResponse;
import com.bytestorm.waygo.application.service.UsuarioService;
import com.bytestorm.waygo.infrastructure.config.security.UsuarioAutenticado;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<PaginaResponse<UsuarioResponse>> listar(
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "" + UsuarioService.TAMANHO_PADRAO) int tamanho) {
		return ResponseEntity.ok(usuarioService.listar(pagina, tamanho));
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UsuarioResponse> eu(Authentication authentication) {
		UsuarioAutenticado usuario = (UsuarioAutenticado) authentication.getPrincipal();
		return ResponseEntity.ok(usuarioService.buscar(usuario.getUsuario().getId()));
	}

}

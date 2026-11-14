package com.bytestorm.waygo.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.request.AlterarSenhaRequest;
import com.bytestorm.waygo.application.dto.response.PaginaResponse;
import com.bytestorm.waygo.application.dto.response.UsuarioResponse;
import com.bytestorm.waygo.application.service.UsuarioService;
import com.bytestorm.waygo.infrastructure.config.security.UsuarioAutenticado;

import jakarta.validation.Valid;

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

	@GetMapping("/eu")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UsuarioResponse> eu(Authentication authentication) {
		UsuarioAutenticado usuario = (UsuarioAutenticado) authentication.getPrincipal();
		return ResponseEntity.ok(usuarioService.buscar(usuario.getUsuario().getId()));
	}

	@PostMapping("/eu/atualizar-senha")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> atualizarSenha(Authentication authentication,
			@Valid @RequestBody AlterarSenhaRequest request) {
		UsuarioAutenticado usuario = (UsuarioAutenticado) authentication.getPrincipal();
		usuarioService.atualizarSenha(usuario.getUsuario().getId(), request);
		return ResponseEntity.noContent().build();
	}

}

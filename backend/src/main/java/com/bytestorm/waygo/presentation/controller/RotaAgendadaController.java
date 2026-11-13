package com.bytestorm.waygo.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.request.RotaAgendadaRequest;
import com.bytestorm.waygo.application.dto.response.RotaAgendadaResponse;
import com.bytestorm.waygo.application.service.RotaAgendadaService;
import com.bytestorm.waygo.infrastructure.config.security.UsuarioAutenticado;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rotas")
public class RotaAgendadaController {

	private final RotaAgendadaService service;

	public RotaAgendadaController(RotaAgendadaService service) {
		this.service = service;
	}

	@GetMapping("/agendamentos")
	public ResponseEntity<List<RotaAgendadaResponse>> listar(Authentication authentication) {
		return ResponseEntity.ok(service.listar(idUsuario(authentication)));
	}

	@GetMapping("/{idRota:\\d+}/agendamentos")
	public ResponseEntity<List<RotaAgendadaResponse>> listarDaRota(@PathVariable Long idRota,
			Authentication authentication) {
		return ResponseEntity.ok(service.listarDaRota(idUsuario(authentication), idRota));
	}

	@PostMapping("/{idRota:\\d+}/agendamentos")
	public ResponseEntity<RotaAgendadaResponse> criar(@PathVariable Long idRota,
			@Valid @RequestBody RotaAgendadaRequest request, Authentication authentication) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(service.criar(idUsuario(authentication), idRota, request));
	}

	@GetMapping("/agendamentos/{id:\\d+}")
	public ResponseEntity<RotaAgendadaResponse> buscar(@PathVariable Long id, Authentication authentication) {
		return ResponseEntity.ok(service.buscar(idUsuario(authentication), id));
	}

	@PutMapping("/agendamentos/{id:\\d+}")
	public ResponseEntity<RotaAgendadaResponse> atualizar(@PathVariable Long id,
			@Valid @RequestBody RotaAgendadaRequest request, Authentication authentication) {
		return ResponseEntity.ok(service.atualizar(idUsuario(authentication), id, request));
	}

	@DeleteMapping("/agendamentos/{id:\\d+}")
	public ResponseEntity<Void> excluir(@PathVariable Long id, Authentication authentication) {
		service.excluir(idUsuario(authentication), id);
		return ResponseEntity.noContent().build();
	}

	private static UUID idUsuario(Authentication authentication) {
		return ((UsuarioAutenticado) authentication.getPrincipal()).getUsuario().getId();
	}

}

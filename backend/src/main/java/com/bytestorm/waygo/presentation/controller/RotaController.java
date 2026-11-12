package com.bytestorm.waygo.presentation.controller;

import java.util.List;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.request.RotaRequest;
import com.bytestorm.waygo.application.dto.request.RotaSalvaRequest;
import com.bytestorm.waygo.application.dto.response.RotaResponse;
import com.bytestorm.waygo.application.dto.response.RotaSalvaResponse;
import com.bytestorm.waygo.application.service.RotaSalvaService;
import com.bytestorm.waygo.application.service.RotaService;
import com.bytestorm.waygo.infrastructure.config.security.UsuarioAutenticado;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rotas")
public class RotaController {

	private final RotaService rotaService;
	private final RotaSalvaService rotaSalvaService;

	public RotaController(RotaService rotaService, RotaSalvaService rotaSalvaService) {
		this.rotaService = rotaService;
		this.rotaSalvaService = rotaSalvaService;
	}

	@GetMapping("/calcular")
	public ResponseEntity<RotaResponse> calcular(@Valid @ParameterObject @ModelAttribute RotaRequest request) {
		return ResponseEntity.ok(rotaService.calcular(request));
	}

	@PostMapping
	public ResponseEntity<RotaSalvaResponse> salvar(@Valid @RequestBody RotaSalvaRequest request,
			Authentication authentication) {
		return ResponseEntity.status(HttpStatus.CREATED).body(rotaSalvaService.salvar(idUsuario(authentication), request));
	}

	@GetMapping
	public ResponseEntity<List<RotaSalvaResponse>> listar(Authentication authentication) {
		return ResponseEntity.ok(rotaSalvaService.listar(idUsuario(authentication)));
	}

	@GetMapping("/{id:\\d+}")
	public ResponseEntity<RotaSalvaResponse> buscar(@PathVariable Long id, Authentication authentication) {
		return ResponseEntity.ok(rotaSalvaService.buscar(idUsuario(authentication), id));
	}

	@PutMapping("/{id:\\d+}")
	public ResponseEntity<RotaSalvaResponse> atualizar(@PathVariable Long id,
			@Valid @RequestBody RotaSalvaRequest request, Authentication authentication) {
		return ResponseEntity.ok(rotaSalvaService.atualizar(idUsuario(authentication), id, request));
	}

	@DeleteMapping("/{id:\\d+}")
	public ResponseEntity<Void> excluir(@PathVariable Long id, Authentication authentication) {
		rotaSalvaService.excluir(idUsuario(authentication), id);
		return ResponseEntity.noContent().build();
	}

	private static UUID idUsuario(Authentication authentication) {
		return ((UsuarioAutenticado) authentication.getPrincipal()).getUsuario().getId();
	}

}

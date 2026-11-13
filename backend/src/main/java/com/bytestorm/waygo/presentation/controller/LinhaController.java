package com.bytestorm.waygo.presentation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.request.LinhaAtualizacaoRequest;
import com.bytestorm.waygo.application.dto.request.LinhaRequest;
import com.bytestorm.waygo.application.dto.request.OrdemEstacoesRequest;
import com.bytestorm.waygo.application.dto.request.StatusLinhaRequest;
import com.bytestorm.waygo.application.dto.response.EstacaoDaLinhaResponse;
import com.bytestorm.waygo.application.dto.response.LinhaDetalheResponse;
import com.bytestorm.waygo.application.dto.response.LinhaResumoResponse;
import com.bytestorm.waygo.application.dto.response.LinhaStatusResponse;
import com.bytestorm.waygo.application.service.AdminLinhaService;
import com.bytestorm.waygo.application.service.LinhaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/linhas")
public class LinhaController {

	private final LinhaService linhaService;
	private final AdminLinhaService adminLinhaService;

	public LinhaController(LinhaService linhaService, AdminLinhaService adminLinhaService) {
		this.linhaService = linhaService;
		this.adminLinhaService = adminLinhaService;
	}

	@GetMapping
	public ResponseEntity<List<LinhaResumoResponse>> listar() {
		return ResponseEntity.ok(linhaService.listar());
	}

	@GetMapping("/{id}")
	public ResponseEntity<LinhaDetalheResponse> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(linhaService.buscarPorId(id));
	}

	@GetMapping("/{id}/estacoes")
	public ResponseEntity<List<EstacaoDaLinhaResponse>> listarEstacoes(@PathVariable Long id) {
		return ResponseEntity.ok(linhaService.listarEstacoes(id));
	}

	@GetMapping("/{id}/status")
	public ResponseEntity<LinhaStatusResponse> buscarStatus(@PathVariable Long id) {
		return ResponseEntity.ok(linhaService.buscarStatusAtual(id));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<LinhaResumoResponse> criar(@Valid @RequestBody LinhaRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(adminLinhaService.criar(request));
	}

	/** Altera nome, cor, operadora e/ou ativa. Código e tipo de transporte não mudam. */
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<LinhaResumoResponse> atualizar(@PathVariable Long id,
			@Valid @RequestBody LinhaAtualizacaoRequest request) {
		return ResponseEntity.ok(adminLinhaService.atualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<Void> desativar(@PathVariable Long id) {
		adminLinhaService.desativar(id);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/{id}/estacoes/ordem")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<List<EstacaoDaLinhaResponse>> reordenarEstacoes(@PathVariable Long id,
			@Valid @RequestBody OrdemEstacoesRequest request) {
		return ResponseEntity.ok(adminLinhaService.reordenarEstacoes(id, request));
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<LinhaStatusResponse> alterarStatus(@PathVariable Long id,
			@Valid @RequestBody StatusLinhaRequest request) {
		return ResponseEntity.ok(adminLinhaService.alterarStatus(id, request));
	}

}

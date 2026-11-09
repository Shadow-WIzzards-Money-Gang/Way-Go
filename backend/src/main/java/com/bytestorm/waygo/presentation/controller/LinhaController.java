package com.bytestorm.waygo.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.response.EstacaoDaLinhaResponse;
import com.bytestorm.waygo.application.dto.response.LinhaDetalheResponse;
import com.bytestorm.waygo.application.dto.response.LinhaResumoResponse;
import com.bytestorm.waygo.application.dto.response.LinhaStatusResponse;
import com.bytestorm.waygo.application.service.LinhaService;

@RestController
@RequestMapping("/linhas")
public class LinhaController {

	private final LinhaService linhaService;

	public LinhaController(LinhaService linhaService) {
		this.linhaService = linhaService;
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

}

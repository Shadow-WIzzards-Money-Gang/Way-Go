package com.bytestorm.waygo.presentation.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.response.ConexaoResponse;
import com.bytestorm.waygo.application.dto.response.EstacaoDetalheResponse;
import com.bytestorm.waygo.application.dto.response.EstacaoResumoResponse;
import com.bytestorm.waygo.application.dto.response.LinhaDaEstacaoResponse;
import com.bytestorm.waygo.application.service.ConexaoService;
import com.bytestorm.waygo.application.service.EstacaoService;

@RestController
@RequestMapping("/estacoes")
public class EstacaoController {

	private final EstacaoService estacaoService;
	private final ConexaoService conexaoService;

	public EstacaoController(EstacaoService estacaoService, ConexaoService conexaoService) {
		this.estacaoService = estacaoService;
		this.conexaoService = conexaoService;
	}

	@GetMapping
	public ResponseEntity<List<EstacaoResumoResponse>> listar() {
		return ResponseEntity.ok(estacaoService.listar());
	}

	@GetMapping("/{id}")
	public ResponseEntity<EstacaoDetalheResponse> buscarPorId(@PathVariable Long id) {
		return ResponseEntity.ok(estacaoService.buscarPorId(id));
	}

	@GetMapping("/{id}/linhas")
	public ResponseEntity<List<LinhaDaEstacaoResponse>> listarLinhas(@PathVariable Long id) {
		return ResponseEntity.ok(estacaoService.listarLinhas(id));
	}

	@GetMapping("/{id}/conexoes")
	public ResponseEntity<List<ConexaoResponse>> listarConexoes(@PathVariable Long id) {
		return ResponseEntity.ok(conexaoService.listarPorEstacao(id));
	}

}

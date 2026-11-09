package com.bytestorm.waygo.presentation.controller;

import java.util.List;

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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/estacoes")
@RequiredArgsConstructor
public class EstacaoController {

	private final EstacaoService estacaoService;
	private final ConexaoService conexaoService;

	@GetMapping
	public List<EstacaoResumoResponse> listar() {
		return estacaoService.listar();
	}

	@GetMapping("/{id}")
	public EstacaoDetalheResponse buscarPorId(@PathVariable Long id) {
		return estacaoService.buscarPorId(id);
	}

	@GetMapping("/{id}/linhas")
	public List<LinhaDaEstacaoResponse> listarLinhas(@PathVariable Long id) {
		return estacaoService.listarLinhas(id);
	}

	@GetMapping("/{id}/conexoes")
	public List<ConexaoResponse> listarConexoes(@PathVariable Long id) {
		return conexaoService.listarPorEstacao(id);
	}

}

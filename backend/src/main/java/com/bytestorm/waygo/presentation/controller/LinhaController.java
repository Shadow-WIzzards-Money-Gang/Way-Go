package com.bytestorm.waygo.presentation.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.response.EstacaoDaLinhaResponse;
import com.bytestorm.waygo.application.dto.response.LinhaDetalheResponse;
import com.bytestorm.waygo.application.dto.response.LinhaResumoResponse;
import com.bytestorm.waygo.application.dto.response.LinhaStatusResponse;
import com.bytestorm.waygo.application.service.LinhaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/linhas")
@RequiredArgsConstructor
public class LinhaController {

	private final LinhaService linhaService;

	@GetMapping
	public List<LinhaResumoResponse> listar() {
		return linhaService.listar();
	}

	@GetMapping("/{id}")
	public LinhaDetalheResponse buscarPorId(@PathVariable Long id) {
		return linhaService.buscarPorId(id);
	}

	@GetMapping("/{id}/estacoes")
	public List<EstacaoDaLinhaResponse> listarEstacoes(@PathVariable Long id) {
		return linhaService.listarEstacoes(id);
	}

	@GetMapping("/{id}/status")
	public LinhaStatusResponse buscarStatus(@PathVariable Long id) {
		return linhaService.buscarStatusAtual(id);
	}

}

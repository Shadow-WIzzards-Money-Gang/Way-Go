package com.bytestorm.waygo.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.response.RotaResponse;
import com.bytestorm.waygo.application.service.RotaService;
import com.bytestorm.waygo.domain.enums.CriterioRota;

@RestController
@RequestMapping("/rotas")
public class RotaController {

	private final RotaService rotaService;

	public RotaController(RotaService rotaService) {
		this.rotaService = rotaService;
	}

	@GetMapping
	public ResponseEntity<RotaResponse> calcular(@RequestParam Long origemId, @RequestParam Long destinoId,
			@RequestParam(required = false) CriterioRota criterio) {
		return ResponseEntity.ok(rotaService.calcular(origemId, destinoId, criterio));
	}

}

package com.bytestorm.waygo.presentation.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.request.RotaRequest;
import com.bytestorm.waygo.application.dto.response.RotaResponse;
import com.bytestorm.waygo.application.service.RotaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rotas")
public class RotaController {

	private final RotaService rotaService;

	public RotaController(RotaService rotaService) {
		this.rotaService = rotaService;
	}

	@GetMapping
	public ResponseEntity<RotaResponse> calcular(@Valid @ParameterObject @ModelAttribute RotaRequest request) {
		return ResponseEntity.ok(rotaService.calcular(request));
	}

}

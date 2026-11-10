package com.bytestorm.waygo.presentation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.response.RedeResponse;
import com.bytestorm.waygo.application.service.RedeService;

@RestController
@RequestMapping("/rede")
public class RedeController {

	private final RedeService redeService;

	public RedeController(RedeService redeService) {
		this.redeService = redeService;
	}

	@GetMapping
	public ResponseEntity<RedeResponse> obter() {
		return ResponseEntity.ok(redeService.obter());
	}

}

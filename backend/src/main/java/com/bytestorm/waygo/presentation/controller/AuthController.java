package com.bytestorm.waygo.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.request.EsqueciSenhaRequest;
import com.bytestorm.waygo.application.dto.request.LoginRequest;
import com.bytestorm.waygo.application.dto.request.RedefinirSenhaRequest;
import com.bytestorm.waygo.application.dto.request.RegistroRequest;
import com.bytestorm.waygo.application.dto.response.LoginResponse;
import com.bytestorm.waygo.application.dto.response.RegistroResponse;
import com.bytestorm.waygo.application.service.AuthService;
import com.bytestorm.waygo.application.service.RecuperacaoSenhaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthService authService;
	private final RecuperacaoSenhaService recuperacaoSenhaService;

	public AuthController(AuthService authService, RecuperacaoSenhaService recuperacaoSenhaService) {
		this.authService = authService;
		this.recuperacaoSenhaService = recuperacaoSenhaService;
	}

	@PostMapping("/registro")
	public ResponseEntity<RegistroResponse> registrar(@Valid @RequestBody RegistroRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return ResponseEntity.ok(authService.login(request));
	}

	@PostMapping("/esqueci-minha-senha")
	public ResponseEntity<Void> esqueciMinhaSenha(@Valid @RequestBody EsqueciSenhaRequest request) {
		recuperacaoSenhaService.solicitar(request.email());
		return ResponseEntity.accepted().build();
	}

	@PostMapping("/redefinir-senha")
	public ResponseEntity<Void> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest request) {
		recuperacaoSenhaService.redefinir(request);
		return ResponseEntity.noContent().build();
	}

}

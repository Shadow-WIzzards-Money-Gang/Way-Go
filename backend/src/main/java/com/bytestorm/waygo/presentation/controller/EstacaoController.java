package com.bytestorm.waygo.presentation.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bytestorm.waygo.application.dto.request.EstacaoRequest;
import com.bytestorm.waygo.application.dto.response.ConexaoResponse;
import com.bytestorm.waygo.application.dto.response.EstacaoDetalheResponse;
import com.bytestorm.waygo.application.dto.response.EstacaoResumoResponse;
import com.bytestorm.waygo.application.dto.response.LinhaDaEstacaoResponse;
import com.bytestorm.waygo.application.dto.response.PaginaResponse;
import com.bytestorm.waygo.application.service.AdminEstacaoService;
import com.bytestorm.waygo.application.service.ConexaoService;
import com.bytestorm.waygo.application.service.EstacaoService;

@RestController
@RequestMapping("/estacoes")
public class EstacaoController {

	private final EstacaoService estacaoService;
	private final ConexaoService conexaoService;
	private final AdminEstacaoService adminEstacaoService;

	public EstacaoController(EstacaoService estacaoService, ConexaoService conexaoService,
			AdminEstacaoService adminEstacaoService) {
		this.estacaoService = estacaoService;
		this.conexaoService = conexaoService;
		this.adminEstacaoService = adminEstacaoService;
	}

	@GetMapping
	public ResponseEntity<PaginaResponse<EstacaoResumoResponse>> listar(
			@RequestParam(defaultValue = "0") int pagina,
			@RequestParam(defaultValue = "" + EstacaoService.TAMANHO_PADRAO) int tamanho,
			@RequestParam(required = false) String busca,
			@RequestParam(defaultValue = "false") boolean integracoes) {
		return ResponseEntity.ok(estacaoService.listar(pagina, tamanho, busca, integracoes));
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

	@PostMapping
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<EstacaoResumoResponse> criar(@RequestBody EstacaoRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(adminEstacaoService.criar(request));
	}

	/** Renomeia, muda a posição (latitude/longitude) e/ou ativa/desativa. */
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<EstacaoResumoResponse> atualizar(@PathVariable Long id,
			@RequestBody EstacaoRequest request) {
		return ResponseEntity.ok(adminEstacaoService.atualizar(id, request));
	}

	/** Desativa a estação (soft delete); um PUT com ativa=true reativa. */
	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMINISTRADOR')")
	public ResponseEntity<Void> desativar(@PathVariable Long id) {
		adminEstacaoService.desativar(id);
		return ResponseEntity.noContent().build();
	}

}

package com.bytestorm.waygo.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.application.dto.response.PaginaResponse;
import com.bytestorm.waygo.application.dto.response.UsuarioResponse;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {

	public static final int TAMANHO_PADRAO = 20;
	public static final int TAMANHO_MAXIMO = 100;

	private final UsuarioRepository usuarioRepository;

	public PaginaResponse<UsuarioResponse> listar(int pagina, int tamanho) {
		if (pagina < 0) {
			throw new RequisicaoInvalidaException("O parâmetro 'pagina' deve ser maior ou igual a 0");
		}
		if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
			throw new RequisicaoInvalidaException("O parâmetro 'tamanho' deve estar entre 1 e " + TAMANHO_MAXIMO);
		}
		return PaginaResponse.de(usuarioRepository.listarPaginado(pagina, tamanho).map(UsuarioResponse::de));
	}

	public UsuarioResponse buscar(UUID id) {
		return usuarioRepository.buscarPorId(id)
				.map(UsuarioResponse::de)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
	}

}

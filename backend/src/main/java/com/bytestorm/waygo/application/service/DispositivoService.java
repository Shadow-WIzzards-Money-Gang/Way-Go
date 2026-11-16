package com.bytestorm.waygo.application.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.application.dto.request.DispositivoRequest;
import com.bytestorm.waygo.domain.entity.DispositivoUsuario;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.DispositivoUsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DispositivoService {

	private final DispositivoUsuarioRepository dispositivoRepository;
	private final Clock clock;

	public void registrar(UUID idUsuario, DispositivoRequest request) {
		LocalDateTime agora = LocalDateTime.now(clock);
		boolean alertasStatus = !Boolean.FALSE.equals(request.alertasStatus());
		try {
			DispositivoUsuario dispositivo = dispositivoRepository.buscarPorToken(request.token().trim())
					.map(existente -> {
						if (!existente.pertenceA(idUsuario)) {
							log.info("Token FCM passou de um usuário para outro (mesmo aparelho, outra conta)");
						}
						existente.registrarNovamente(idUsuario, request.plataforma(), alertasStatus, agora);
						return existente;
					})
					.orElseGet(() -> DispositivoUsuario.criar(idUsuario, request.token().trim(),
							request.plataforma(), alertasStatus, agora));
			dispositivoRepository.salvar(dispositivo);
		} catch (IllegalArgumentException e) {
			throw new RequisicaoInvalidaException(e.getMessage());
		}
	}

	/** Ao sair da conta: o aparelho deixa de receber as notificações dela. */
	public void remover(UUID idUsuario, String token) {
		if (token == null || token.isBlank()) {
			throw new RequisicaoInvalidaException("O token é obrigatório");
		}
		dispositivoRepository.excluirPorTokenEUsuario(token.trim(), idUsuario);
	}

}

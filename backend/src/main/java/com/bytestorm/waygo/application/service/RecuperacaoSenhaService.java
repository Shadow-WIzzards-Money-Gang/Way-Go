package com.bytestorm.waygo.application.service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bytestorm.waygo.application.dto.request.RedefinirSenhaRequest;
import com.bytestorm.waygo.domain.entity.TokenRecuperacaoSenha;
import com.bytestorm.waygo.domain.entity.Usuario;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.interfaces.EnviadorEmail;
import com.bytestorm.waygo.domain.interfaces.GeradorCodigo;
import com.bytestorm.waygo.domain.interfaces.SenhaHasher;
import com.bytestorm.waygo.domain.interfaces.TokenRecuperacaoSenhaRepository;
import com.bytestorm.waygo.domain.interfaces.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Recuperação de senha por código de 6 dígitos enviado por e-mail.
 * As respostas não revelam se o e-mail tem conta: o pedido sempre "dá certo" e
 * qualquer falha na redefinição devolve a mesma mensagem.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

	static final Duration INTERVALO_REENVIO = Duration.ofMinutes(1);
	static final String MENSAGEM_CODIGO_INVALIDO = "Código inválido ou expirado";

	private final UsuarioRepository usuarioRepository;
	private final TokenRecuperacaoSenhaRepository tokenRepository;
	private final SenhaHasher senhaHasher;
	private final GeradorCodigo geradorCodigo;
	private final EnviadorEmail enviadorEmail;
	private final Clock clock;

	@Transactional
	public void solicitar(String email) {
		Optional<Usuario> encontrado = usuarioRepository.buscarPorEmail(email);
		if (encontrado.isEmpty()) {
			log.info("Recuperação de senha pedida para e-mail sem conta; nada enviado");
			return;
		}
		Usuario usuario = encontrado.get();
		LocalDateTime agora = LocalDateTime.now(clock);

		boolean pedidoRecente = tokenRepository.buscarMaisRecenteDoUsuario(usuario.getId())
				.filter(t -> t.foiCriadoHaMenosDe(INTERVALO_REENVIO, agora))
				.isPresent();
		if (pedidoRecente) {
			log.info("Recuperação de senha pedida de novo em menos de {}s para {}; reenvio ignorado",
					INTERVALO_REENVIO.toSeconds(), usuario.getEmail());
			return;
		}

		tokenRepository.invalidarAtivosDoUsuario(usuario.getId(), agora);
		String codigo = geradorCodigo.gerar();
		tokenRepository.salvar(TokenRecuperacaoSenha.criar(usuario.getId(), codigo, senhaHasher, agora));
		enviadorEmail.enviarCodigoRecuperacao(usuario.getEmail(), codigo);
	}

	@Transactional(noRollbackFor = RequisicaoInvalidaException.class)
	public void redefinir(RedefinirSenhaRequest request) {
		LocalDateTime agora = LocalDateTime.now(clock);
		Usuario usuario = usuarioRepository.buscarPorEmail(request.email())
				.orElseThrow(() -> new RequisicaoInvalidaException(MENSAGEM_CODIGO_INVALIDO));
		TokenRecuperacaoSenha token = tokenRepository.buscarMaisRecenteDoUsuario(usuario.getId())
				.filter(t -> t.estaValido(agora))
				.orElseThrow(() -> new RequisicaoInvalidaException(MENSAGEM_CODIGO_INVALIDO));

		if (!token.confere(request.codigo(), senhaHasher)) {
			token.registrarTentativaFalha();
			tokenRepository.salvar(token);
			log.info("Código de recuperação incorreto para {} ({} de {} tentativas)", usuario.getEmail(),
					token.getTentativas(), TokenRecuperacaoSenha.MAX_TENTATIVAS);
			throw new RequisicaoInvalidaException(MENSAGEM_CODIGO_INVALIDO);
		}

		try {
			usuario.redefinirSenha(request.novaSenha(), senhaHasher);
		} catch (IllegalArgumentException e) {
			// Senha fora das regras: o código continua valendo para uma nova tentativa.
			throw new RequisicaoInvalidaException(e.getMessage());
		}
		token.marcarUsado(agora);
		tokenRepository.salvar(token);
		usuarioRepository.salvar(usuario);
		log.info("Senha redefinida por código de recuperação para {}", usuario.getEmail());
		enviadorEmail.enviarSenhaRedefinida(usuario.getEmail());
	}

}

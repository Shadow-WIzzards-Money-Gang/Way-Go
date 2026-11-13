package com.bytestorm.waygo.presentation.handler;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.bytestorm.waygo.application.dto.response.ErroResponse;
import com.bytestorm.waygo.domain.exception.ConflitoException;
import com.bytestorm.waygo.domain.exception.CredenciaisInvalidasException;
import com.bytestorm.waygo.domain.exception.EmailJaCadastradoException;
import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;
import com.bytestorm.waygo.domain.exception.RequisicaoInvalidaException;
import com.bytestorm.waygo.domain.exception.RotaNaoEncontradaException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private final Clock clock;

	public GlobalExceptionHandler(Clock clock) {
		this.clock = clock;
	}

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ResponseEntity<ErroResponse> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
		return erro(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(RotaNaoEncontradaException.class)
	public ResponseEntity<ErroResponse> tratarRotaNaoEncontrada(RotaNaoEncontradaException ex) {
		return erro(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
	}

	@ExceptionHandler(CredenciaisInvalidasException.class)
	public ResponseEntity<ErroResponse> tratarCredenciaisInvalidas(CredenciaisInvalidasException ex) {
		return erro(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	@ExceptionHandler(EmailJaCadastradoException.class)
	public ResponseEntity<ErroResponse> tratarEmailJaCadastrado(EmailJaCadastradoException ex) {
		return erro(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(ConflitoException.class)
	public ResponseEntity<ErroResponse> tratarConflito(ConflitoException ex) {
		return erro(HttpStatus.CONFLICT, ex.getMessage());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErroResponse> tratarAcessoNegado(AccessDeniedException ex) {
		return erro(HttpStatus.FORBIDDEN, "Acesso restrito a administradores");
	}

	@ExceptionHandler(RequisicaoInvalidaException.class)
	public ResponseEntity<ErroResponse> tratarRequisicaoInvalida(RequisicaoInvalidaException ex) {
		return erro(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ErroResponse> tratarParametroAusente(MissingServletRequestParameterException ex) {
		return erro(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente: '" + ex.getParameterName() + "'");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErroResponse> tratarParametroInvalido(MethodArgumentTypeMismatchException ex) {
		return erro(HttpStatus.BAD_REQUEST,
				"Valor inválido para o parâmetro '" + ex.getName() + "': " + ex.getValue());
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException ex) {
		return erro(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou com JSON inválido");
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ErroResponse> tratarValidacao(BindException ex) {
		String mensagem = ex.getFieldErrors().stream()
				.map(FieldError::getDefaultMessage)
				.collect(Collectors.joining("; "));
		return erro(HttpStatus.BAD_REQUEST, mensagem.isEmpty() ? "Requisição inválida" : mensagem);
	}

	private ResponseEntity<ErroResponse> erro(HttpStatus status, String message) {
		return ResponseEntity.status(status)
				.body(new ErroResponse(status.value(), message, LocalDateTime.now(clock)));
	}

}

package com.bytestorm.waygo.presentation.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.bytestorm.waygo.domain.exception.RecursoNaoEncontradoException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RecursoNaoEncontradoException.class)
	public ProblemDetail tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problema.setTitle("Recurso não encontrado");
		return problema;
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ProblemDetail tratarParametroInvalido(MethodArgumentTypeMismatchException ex) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
				"Valor inválido para o parâmetro '" + ex.getName() + "': " + ex.getValue());
		problema.setTitle("Parâmetro inválido");
		return problema;
	}

}

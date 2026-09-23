package br.insper.curso.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus faz o Spring devolver 404 automaticamente quando a exception sobe do service.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CursoNaoEncontradoException extends RuntimeException {

	public CursoNaoEncontradoException(String mensagem) {
		super(mensagem);
	}
}

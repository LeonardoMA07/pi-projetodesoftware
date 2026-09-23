package br.insper.avaliacao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus faz o Spring devolver 404 automaticamente quando a exception sobe do service.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AvaliacaoNaoEncontradaException extends RuntimeException {

	public AvaliacaoNaoEncontradaException(String mensagem) {
		super(mensagem);
	}
}

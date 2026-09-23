package br.insper.avaliacao.controller;

import br.insper.avaliacao.dto.AvaliacaoDto;
import br.insper.avaliacao.entity.Avaliacao;
import br.insper.avaliacao.service.AvaliacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/avaliacao")
public class AvaliacaoController {

	@Autowired
	private AvaliacaoService avaliacaoService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Avaliacao criar(@RequestBody AvaliacaoDto dto) {
		return avaliacaoService.criar(dto);
	}

	// GET /avaliacao              -> todas as avaliacoes
	// GET /avaliacao?autor=Maria  -> apenas as que o autor comeca com "Maria"
	@GetMapping
	public List<Avaliacao> listar(@RequestParam(required = false) String autor) {
		return avaliacaoService.listar(autor);
	}

	// GET /avaliacao/{id} -> uma avaliacao; 404 se nao existir
	@GetMapping("/{id}")
	public Avaliacao buscarPorId(@PathVariable Long id) {
		return avaliacaoService.buscarPorId(id);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletar(@PathVariable Long id) {
		avaliacaoService.deletar(id);
	}

}

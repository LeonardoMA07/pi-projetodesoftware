package br.insper.avaliacao.controller;

import br.insper.avaliacao.dto.AvaliacaoDto;
import br.insper.avaliacao.entity.Avaliacao;
import br.insper.avaliacao.service.AvaliacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cursos")
public class AvaliacaoController {

	@Autowired
	private AvaliacaoService avaliacaoService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Avaliacao criar(@RequestBody AvaliacaoDto dto) {
		return avaliacaoService.criar(dto);
	}

	// GET /cursos            -> todos os cursos nao deletados
	// GET /cursos?nome=Java  -> apenas os que o nome comeca com "Java"
	@GetMapping
	public List<Avaliacao> listar(@RequestParam(required = false) String nome) {
		return avaliacaoService.listar(autor);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletar(@PathVariable Long id) {
		avaliacaoService.deletar(id);
	}

}

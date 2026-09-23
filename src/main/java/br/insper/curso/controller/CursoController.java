package br.insper.curso.controller;

import br.insper.curso.dto.CursoDto;
import br.insper.curso.entity.Curso;
import br.insper.curso.service.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cursos")
public class CursoController {

	@Autowired
	private CursoService cursoService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Curso criar(@RequestBody CursoDto dto) {
		return cursoService.criar(dto);
	}

	// GET /cursos            -> todos os cursos nao deletados
	// GET /cursos?nome=Java  -> apenas os que o nome comeca com "Java"
	@GetMapping
	public List<Curso> listar(@RequestParam(required = false) String nome) {
		return cursoService.listar(nome);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletar(@PathVariable Long id) {
		cursoService.deletar(id);
	}

}

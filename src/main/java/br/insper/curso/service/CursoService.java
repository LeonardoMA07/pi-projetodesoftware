package br.insper.curso.service;

import br.insper.curso.dto.CursoDto;
import br.insper.curso.entity.Curso;
import br.insper.curso.exception.CursoNaoEncontradoException;
import br.insper.curso.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CursoService {

	@Autowired
	private CursoRepository cursoRepository;

	public Curso criar(CursoDto dto) {
		Curso curso = Curso.fromDto(dto);
		return cursoRepository.save(curso);
	}

	public List<Curso> listar(String nome) {
		if (nome != null && !nome.isBlank()) {
			return cursoRepository.findByNomeStartingWithIgnoreCaseAndDeletadoFalse(nome);
		}
		return cursoRepository.findByDeletadoFalse();
	}

	public void deletar(Long id) {
		Curso curso = cursoRepository
				.findById(id)
				.orElseThrow(() -> new CursoNaoEncontradoException("Curso com ID " + id + " nao encontrado"));

		// Deleção lógica: marca a flag e salva, o registro continua no banco.
		curso.setDeletado(true);
		cursoRepository.save(curso);
	}
}

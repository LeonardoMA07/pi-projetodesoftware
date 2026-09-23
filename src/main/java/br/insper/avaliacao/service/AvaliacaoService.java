package br.insper.avaliacao.service;

import br.insper.avaliacao.dto.AvaliacaoDto;
import br.insper.avaliacao.entity.Avaliacao;
import br.insper.avaliacao.exception.AvaliacaoNaoEncontradaException;
import br.insper.avaliacao.repository.AvaliacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvaliacaoService {

	@Autowired
	private AvaliacaoRepository cursoRepository;

	public Avaliacao criar(AvaliacaoDto dto) {
		Avaliacao avaliacao = Avaliacao.fromDto(dto);
		return cursoRepository.save(avaliacao);
	}

	public List<Avaliacao> listar(String nome) {
		if (nome != null && !nome.isBlank()) {
			return cursoRepository.findByNomeStartingWithIgnoreCaseAndDeletadoFalse(nome);
		}
		return cursoRepository.findByDeletadoFalse();
	}

	public void deletar(Long id) {
		Avaliacao avaliacao = cursoRepository
				.findById(id)
				.orElseThrow(() -> new AvaliacaoNaoEncontradaException("Avaliacao com ID " + id + " nao encontrada"));

		cursoRepository.delete(avaliacao);
	}
}

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
	private AvaliacaoRepository avaliacaoRepository;

	public Avaliacao criar(AvaliacaoDto dto) {
		Avaliacao avaliacao = Avaliacao.fromDto(dto);
		return avaliacaoRepository.save(avaliacao);
	}

	public List<Avaliacao> listar(String autor) {
		if (autor != null && !autor.isBlank()) {
			return avaliacaoRepository.findByAutorStartingWithIgnoreCase(autor);
		}
		return avaliacaoRepository.findAll();
	}

	public Avaliacao buscarPorId(Long id) {
		return avaliacaoRepository
				.findById(id)
				.orElseThrow(() -> new AvaliacaoNaoEncontradaException("Avaliacao com ID " + id + " nao encontrada"));
	}

	public void deletar(Long id) {
		Avaliacao avaliacao = buscarPorId(id);
		avaliacaoRepository.delete(avaliacao);
	}
}

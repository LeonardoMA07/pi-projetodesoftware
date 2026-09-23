package br.insper.avaliacao.repository;

import br.insper.avaliacao.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

	// Spring Data monta a query pelo nome do metodo: filtro startsWith no autor,
	// ignorando maiusculas/minusculas. findAll() e findById() ja vem do JpaRepository.
	List<Avaliacao> findByAutorStartingWithIgnoreCase(String autor);
}

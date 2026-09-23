package br.insper.avaliacao.repository;

import br.insper.avaliacao.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

	// Spring Data monta a query pelo nome do metodo: todos os cursos nao deletados.
	List<Avaliacao> findByDeletadoFalse();

	// Filtro startsWith (ignorando maiusculas/minusculas), tambem sem os deletados.
	List<Avaliacao> findByNomeStartingWithIgnoreCaseAndDeletadoFalse(String nome);
}

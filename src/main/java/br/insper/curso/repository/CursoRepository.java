package br.insper.curso.repository;

import br.insper.curso.entity.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {

	// Spring Data monta a query pelo nome do metodo: todos os cursos nao deletados.
	List<Curso> findByDeletadoFalse();

	// Filtro startsWith (ignorando maiusculas/minusculas), tambem sem os deletados.
	List<Curso> findByNomeStartingWithIgnoreCaseAndDeletadoFalse(String nome);
}

package br.insper.curso.entity;

import br.insper.curso.dto.CursoDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cursos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Curso {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nome;

	@Column
	private String descricao;

	@Column
	private String instrutor;

	@Column
	private Integer cargaHoraria;

	@Column
	private BigDecimal preco;

	@Column
	private LocalDate dataCriacao;

	// Deleção lógica: o registro continua no banco, apenas marcado como deletado.
	@Column(nullable = false)
	private Boolean deletado;

	public static Curso fromDto(CursoDto dto) {
		Curso curso = new Curso();
		curso.setNome(dto.getNome());
		curso.setDescricao(dto.getDescricao());
		curso.setInstrutor(dto.getInstrutor());
		curso.setCargaHoraria(dto.getCargaHoraria());
		curso.setPreco(dto.getPreco());
		curso.setDataCriacao(LocalDate.now());
		curso.setDeletado(false);
		return curso;
	}
}

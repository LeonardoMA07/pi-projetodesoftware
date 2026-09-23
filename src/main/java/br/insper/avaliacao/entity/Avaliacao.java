package br.insper.avaliacao.entity;

import br.insper.avaliacao.dto.AvaliacaoDto;
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
public class Avaliacao

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String autor;

	@Column
	private String conteudo;

	@Column
	@Min(value=1, message = "O valor mínimo permitido é 1")
	@Max(value=5, message= "O valor máximo permitido é 5")
	private int nota;

	@Column
	private LocalDate dataAvaliacao;

	public static Avaliacao fromDto(AvaliacaoDto dto) {
		Avaliacao avaliacao = new Avaliacao();
		avaliacao.setAutor(dto.getNome());
		avaliacao.setConteudo(dto.getDescricao());
		avaliacao.setNota(dto.getPreco());
		avaliacao.setDataAvaliacao(LocalDate.now());
		return avaliacao;
	}
}

package br.insper.avaliacao.entity;

import br.insper.avaliacao.dto.AvaliacaoDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "avaliacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Avaliacao{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String autor;

	@Column
	private String conteudo;

	@Column
	private int nota;

	@Column
	private LocalDate dataAvaliacao;

	public static Avaliacao fromDto(AvaliacaoDto dto) {
		Avaliacao avaliacao = new Avaliacao();
		avaliacao.setAutor(dto.getAutor());
		avaliacao.setConteudo(dto.getConteudo());
		avaliacao.setNota(dto.getNota());
		avaliacao.setDataAvaliacao(LocalDate.now());
		return avaliacao;
	}
}

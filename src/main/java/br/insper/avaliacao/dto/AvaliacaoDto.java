package br.insper.avaliacao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoDto {
	private String autor;
	private String conteudo;
	private Integer nota;
}

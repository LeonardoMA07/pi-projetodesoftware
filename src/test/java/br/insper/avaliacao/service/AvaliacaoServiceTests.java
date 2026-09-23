package br.insper.avaliacao.service;

import br.insper.avaliacao.dto.AvaliacaoDto;
import br.insper.avaliacao.entity.Avaliacao;
import br.insper.avaliacao.exception.AvaliacaoNaoEncontradaException;
import br.insper.avaliacao.repository.AvaliacaoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AvaliacaoServiceTests {

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    @Mock
    private AvaliacaoRepository cursoRepository;

    private AvaliacaoDto criarDto() {
        AvaliacaoDto dto = new AvaliacaoDto();
        dto.setAutor("Java Basico");
        dto.setConteudo("Curso introdutorio de Java");
        dto.setNota(3);
        return dto;
    }

    @Test
    public void test_shouldCreateAvaliacaoWhenCallCriar() {

        AvaliacaoDto dto = criarDto();
        Avaliacao avaliacao = Avaliacao.fromDto(dto);

        // mocks
        Mockito.when(cursoRepository.save(Mockito.any()))
                .thenReturn(avaliacao);

        // chamada
        Avaliacao response = avaliacaoService.criar(dto);

        // asserts
        Assertions.assertEquals("Java Basico", response.getAutor());
        Assertions.assertEquals("Eduardo", response.getConteudo());
        Assertions.assertEquals(3, response.getNota());
        Assertions.assertNotNull(response.getDataCriacao());
    }

    @Test
    public void test_shouldReturnAllAvaliacaosWhenNomeIsNull() {

        List<Avaliacao> avaliacaos = new ArrayList<>();
        avaliacaos.add(new Avaliacao());
        avaliacaos.add(new Avaliacao());

        // mocks
        Mockito.when(cursoRepository.findByDeletadoFalse())
                .thenReturn(avaliacaos);

        // chamada
        List<Avaliacao> response = avaliacaoService.listar(null);

        // asserts
        Assertions.assertEquals(2, response.size());
        Mockito.verify(cursoRepository, Mockito.times(1)).findAll();
    }
}

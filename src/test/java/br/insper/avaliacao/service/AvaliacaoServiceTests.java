package br.insper.avaliacao.service;

import br.insper.avaliacao.dto.AvaliacaoDto;
import br.insper.avaliacao.entity.Avaliacao;
import br.insper.avaliacao.exception.AvaliacaoNaoEncontradaException;
import br.insper.avaliacao.repository.AvaliacaoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AvaliacaoServiceTests {

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

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
        Mockito.when(avaliacaoRepository.save(Mockito.any()))
                .thenReturn(avaliacao);

        // chamada
        Avaliacao response = avaliacaoService.criar(dto);

        // asserts
        Assertions.assertEquals("Java Basico", response.getAutor());
        Assertions.assertEquals("Curso introdutorio de Java", response.getConteudo());
        Assertions.assertEquals(3, response.getNota());
        Assertions.assertNotNull(response.getDataAvaliacao());
    }

    @Test
    public void test_shouldReturnAllAvaliacoesWhenAutorIsNull() {

        List<Avaliacao> avaliacoes = new ArrayList<>();
        avaliacoes.add(new Avaliacao());
        avaliacoes.add(new Avaliacao());

        // mocks
        Mockito.when(avaliacaoRepository.findAll())
                .thenReturn(avaliacoes);

        // chamada
        List<Avaliacao> response = avaliacaoService.listar(null);

        // asserts
        Assertions.assertEquals(2, response.size());
        Mockito.verify(avaliacaoRepository, Mockito.times(1)).findAll();
    }

    @Test
    public void test_shouldFilterByAutorWhenAutorIsInformed() {

        List<Avaliacao> avaliacoes = new ArrayList<>();
        avaliacoes.add(new Avaliacao());

        // mocks
        Mockito.when(avaliacaoRepository.findByAutorStartingWithIgnoreCase("Java"))
                .thenReturn(avaliacoes);

        // chamada
        List<Avaliacao> response = avaliacaoService.listar("Java");

        // asserts
        Assertions.assertEquals(1, response.size());
        Mockito.verify(avaliacaoRepository, Mockito.never()).findAll();
    }

    @Test
    public void test_shouldReturnAvaliacaoWhenCallBuscarPorId() {

        Avaliacao avaliacao = Avaliacao.fromDto(criarDto());
        avaliacao.setId(1L);

        // mocks
        Mockito.when(avaliacaoRepository.findById(1L))
                .thenReturn(Optional.of(avaliacao));

        // chamada
        Avaliacao response = avaliacaoService.buscarPorId(1L);

        // asserts
        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals("Java Basico", response.getAutor());
    }

    @Test
    public void test_shouldThrowExceptionWhenBuscarPorIdNotFound() {

        // mocks
        Mockito.when(avaliacaoRepository.findById(99L))
                .thenReturn(Optional.empty());

        // chamada + assert
        Assertions.assertThrows(AvaliacaoNaoEncontradaException.class,
                () -> avaliacaoService.buscarPorId(99L));
    }

    @Test
    public void test_shouldDeleteAvaliacaoWhenCallDeletar() {

        Avaliacao avaliacao = Avaliacao.fromDto(criarDto());
        avaliacao.setId(1L);

        // mocks
        Mockito.when(avaliacaoRepository.findById(1L))
                .thenReturn(Optional.of(avaliacao));

        // chamada
        avaliacaoService.deletar(1L);

        // asserts
        Mockito.verify(avaliacaoRepository, Mockito.times(1)).delete(avaliacao);
    }

    @Test
    public void test_shouldThrowExceptionWhenDeletarNotFound() {

        // mocks
        Mockito.when(avaliacaoRepository.findById(99L))
                .thenReturn(Optional.empty());

        // chamada + assert
        Assertions.assertThrows(AvaliacaoNaoEncontradaException.class,
                () -> avaliacaoService.deletar(99L));
        Mockito.verify(avaliacaoRepository, Mockito.never()).delete(Mockito.any());
    }
}

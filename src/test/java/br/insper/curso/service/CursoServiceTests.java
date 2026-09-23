package br.insper.curso.service;

import br.insper.curso.dto.CursoDto;
import br.insper.curso.entity.Curso;
import br.insper.curso.exception.CursoNaoEncontradoException;
import br.insper.curso.repository.CursoRepository;
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
public class CursoServiceTests {

    @InjectMocks
    private CursoService cursoService;

    @Mock
    private CursoRepository cursoRepository;

    private CursoDto criarDto() {
        CursoDto dto = new CursoDto();
        dto.setNome("Java Basico");
        dto.setDescricao("Curso introdutorio de Java");
        dto.setInstrutor("Eduardo");
        dto.setCargaHoraria(40);
        dto.setPreco(new BigDecimal(500));
        return dto;
    }

    @Test
    public void test_shouldCreateCursoWhenCallCriar() {

        CursoDto dto = criarDto();
        Curso curso = Curso.fromDto(dto);

        // mocks
        Mockito.when(cursoRepository.save(Mockito.any()))
                .thenReturn(curso);

        // chamada
        Curso response = cursoService.criar(dto);

        // asserts
        Assertions.assertEquals("Java Basico", response.getNome());
        Assertions.assertEquals("Eduardo", response.getInstrutor());
        Assertions.assertEquals(40, response.getCargaHoraria());
        Assertions.assertFalse(response.getDeletado());
        Assertions.assertNotNull(response.getDataCriacao());
    }

    @Test
    public void test_shouldReturnAllCursosWhenNomeIsNull() {

        List<Curso> cursos = new ArrayList<>();
        cursos.add(new Curso());
        cursos.add(new Curso());

        // mocks
        Mockito.when(cursoRepository.findByDeletadoFalse())
                .thenReturn(cursos);

        // chamada
        List<Curso> response = cursoService.listar(null);

        // asserts
        Assertions.assertEquals(2, response.size());
        Mockito.verify(cursoRepository, Mockito.times(1)).findByDeletadoFalse();
    }

    @Test
    public void test_shouldReturnAllCursosWhenNomeIsBlank() {

        List<Curso> cursos = new ArrayList<>();
        cursos.add(new Curso());

        // mocks
        Mockito.when(cursoRepository.findByDeletadoFalse())
                .thenReturn(cursos);

        // chamada
        List<Curso> response = cursoService.listar("   ");

        // asserts
        Assertions.assertEquals(1, response.size());
        Mockito.verify(cursoRepository, Mockito.times(1)).findByDeletadoFalse();
    }

    @Test
    public void test_shouldReturnFilteredCursosWhenNomeIsInformed() {

        Curso curso = Curso.fromDto(criarDto());

        List<Curso> cursos = new ArrayList<>();
        cursos.add(curso);

        // mocks
        Mockito.when(cursoRepository.findByNomeStartingWithIgnoreCaseAndDeletadoFalse("Java"))
                .thenReturn(cursos);

        // chamada
        List<Curso> response = cursoService.listar("Java");

        // asserts
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals("Java Basico", response.getFirst().getNome());
        Mockito.verify(cursoRepository, Mockito.never()).findByDeletadoFalse();
    }

    @Test
    public void test_shouldMarkCursoAsDeletedWhenCallDeletar() {

        Curso curso = Curso.fromDto(criarDto());
        curso.setId(1L);

        // mocks
        Mockito.when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(curso));

        // chamada
        cursoService.deletar(1L);

        // asserts: o curso foi salvo com a flag deletado = true (delecao logica)
        ArgumentCaptor<Curso> captor = ArgumentCaptor.forClass(Curso.class);
        Mockito.verify(cursoRepository, Mockito.times(1)).save(captor.capture());
        Assertions.assertTrue(captor.getValue().getDeletado());

        // garante que nenhuma delecao fisica aconteceu
        Mockito.verify(cursoRepository, Mockito.never()).deleteById(Mockito.any());
    }

    @Test
    public void test_shouldThrowExceptionWhenDeletarCursoNaoExiste() {

        // mocks
        Mockito.when(cursoRepository.findById(99L))
                .thenReturn(Optional.empty());

        // chamada + assert
        CursoNaoEncontradoException exception = Assertions.assertThrows(
                CursoNaoEncontradoException.class,
                () -> cursoService.deletar(99L));

        Assertions.assertEquals("Curso com ID 99 nao encontrado", exception.getMessage());
        Mockito.verify(cursoRepository, Mockito.never()).save(Mockito.any());
    }

}

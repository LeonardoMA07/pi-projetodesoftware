package br.insper.curso.controller;

import br.insper.curso.dto.CursoDto;
import br.insper.curso.entity.Curso;
import br.insper.curso.repository.CursoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class CursoControllerTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("curso_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CursoRepository cursoRepository;

    @BeforeEach
    public void limparBase() {
        cursoRepository.deleteAll();
    }

    private CursoDto criarDto(String nome) {
        CursoDto dto = new CursoDto();
        dto.setNome(nome);
        dto.setDescricao("Descricao de " + nome);
        dto.setInstrutor("Eduardo");
        dto.setCargaHoraria(40);
        dto.setPreco(new BigDecimal(500));
        return dto;
    }

    private Curso postCurso(String nome) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/cursos")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(criarDto(nome))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), Curso.class);
    }

    // ---------- POST /cursos ----------

    @Test
    public void test_shouldCreateCurso() throws Exception {

        // chamada
        MvcResult result = mockMvc.perform(
                        post("/cursos")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(criarDto("Java Basico"))))
                .andExpect(status().isCreated())
                .andReturn();

        // asserts
        Curso curso = objectMapper.readValue(result.getResponse().getContentAsString(), Curso.class);
        Assertions.assertNotNull(curso.getId());
        Assertions.assertEquals("Java Basico", curso.getNome());
        Assertions.assertEquals("Eduardo", curso.getInstrutor());
        Assertions.assertEquals(40, curso.getCargaHoraria());
        Assertions.assertFalse(curso.getDeletado());
    }

    // ---------- GET /cursos ----------

    @Test
    public void test_shouldListAllCursos() throws Exception {

        postCurso("Java Basico");
        postCurso("Python Avancado");

        // chamada
        MvcResult result = mockMvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andReturn();

        // asserts
        List<Curso> cursos = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Curso.class));

        Assertions.assertEquals(2, cursos.size());
    }

    @Test
    public void test_shouldFilterCursosByNomeStartingWith() throws Exception {

        postCurso("Java Basico");
        postCurso("Java Avancado");
        postCurso("Python Basico");

        // chamada
        MvcResult result = mockMvc.perform(get("/cursos").param("nome", "Java"))
                .andExpect(status().isOk())
                .andReturn();

        // asserts: apenas os dois cursos que comecam com "Java"
        List<Curso> cursos = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Curso.class));

        Assertions.assertEquals(2, cursos.size());
        Assertions.assertTrue(cursos.stream().allMatch(c -> c.getNome().startsWith("Java")));
    }

    // ---------- DELETE /cursos/{id} ----------

    @Test
    public void test_shouldDeleteCursoLogicallyAndHideItFromList() throws Exception {

        Curso curso = postCurso("Java Basico");
        postCurso("Python Basico");

        // chamada
        mockMvc.perform(delete("/cursos/" + curso.getId()))
                .andExpect(status().isNoContent());

        // assert 1: o curso deletado nao aparece mais na listagem
        MvcResult result = mockMvc.perform(get("/cursos"))
                .andExpect(status().isOk())
                .andReturn();

        List<Curso> cursos = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Curso.class));

        Assertions.assertEquals(1, cursos.size());
        Assertions.assertEquals("Python Basico", cursos.getFirst().getNome());

        // assert 2: a delecao foi apenas logica, o registro continua no banco
        Curso doBanco = cursoRepository.findById(curso.getId()).orElseThrow();
        Assertions.assertTrue(doBanco.getDeletado());
    }

    @Test
    public void test_shouldReturnNotFoundWhenDeleteCursoInexistente() throws Exception {

        mockMvc.perform(delete("/cursos/99999"))
                .andExpect(status().isNotFound());
    }

}

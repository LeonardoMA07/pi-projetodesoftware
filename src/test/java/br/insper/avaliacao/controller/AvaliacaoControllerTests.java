package br.insper.avaliacao.controller;

import br.insper.avaliacao.dto.AvaliacaoDto;
import br.insper.avaliacao.entity.Avaliacao;
import br.insper.avaliacao.repository.AvaliacaoRepository;
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
public class AvaliacaoControllerTests {

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
    private AvaliacaoRepository cursoRepository;

    @BeforeEach
    public void limparBase() {
        cursoRepository.deleteAll();
    }

    private AvaliacaoDto criarDto(String autor) {
        AvaliacaoDto dto = new AvaliacaoDto();
        dto.setAutor(autor);
        dto.setConteudo("Descricao de " + autor);
        dto.setNota(3);
        return dto;
    }

    private Avaliacao postCurso(String nome) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/cursos")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(criarDto(nome))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), Avaliacao.class);
    }

    // ---------- POST /cursos ----------

    @Test
    public void test_shouldCreateAvaliacao() throws Exception {

        // chamada
        MvcResult result = mockMvc.perform(
                        post("/cursos")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(criarDto("Java Basico"))))
                .andExpect(status().isCreated())
                .andReturn();

        // asserts
        Avaliacao avaliacao = objectMapper.readValue(result.getResponse().getContentAsString(), Avaliacao.class);
        Assertions.assertNotNull(avaliacao.getId());
        Assertions.assertEquals("Java Basico", avaliacao.getAutor());
        Assertions.assertEquals("Eduardo", avaliacao.getDescricao());
        Assertions.assertEquals(3, avaliacao.getNota());
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
        List<Avaliacao> avaliacaos = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Avaliacao.class));

        Assertions.assertEquals(2, avaliacaos.size());
    }

}

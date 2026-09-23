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
    private AvaliacaoRepository avaliacaoRepository;

    @BeforeEach
    public void limparBase() {
        avaliacaoRepository.deleteAll();
    }

    private AvaliacaoDto criarDto(String autor) {
        AvaliacaoDto dto = new AvaliacaoDto();
        dto.setAutor(autor);
        dto.setConteudo("Descricao de " + autor);
        dto.setNota(3);
        return dto;
    }

    private Avaliacao postAvaliacao(String autor) throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/avaliacao")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(criarDto(autor))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), Avaliacao.class);
    }

    // ---------- POST /avaliacao ----------

    @Test
    public void test_shouldCreateAvaliacao() throws Exception {

        // chamada
        MvcResult result = mockMvc.perform(
                        post("/avaliacao")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(criarDto("Java Basico"))))
                .andExpect(status().isCreated())
                .andReturn();

        // asserts
        Avaliacao avaliacao = objectMapper.readValue(result.getResponse().getContentAsString(), Avaliacao.class);
        Assertions.assertNotNull(avaliacao.getId());
        Assertions.assertEquals("Java Basico", avaliacao.getAutor());
        Assertions.assertEquals("Descricao de Java Basico", avaliacao.getConteudo());
        Assertions.assertEquals(3, avaliacao.getNota());
    }

    // ---------- GET /avaliacao ----------

    @Test
    public void test_shouldListAllAvaliacoes() throws Exception {

        postAvaliacao("Java Basico");
        postAvaliacao("Python Avancado");

        // chamada
        MvcResult result = mockMvc.perform(get("/avaliacao"))
                .andExpect(status().isOk())
                .andReturn();

        // asserts
        List<Avaliacao> avaliacoes = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Avaliacao.class));

        Assertions.assertEquals(2, avaliacoes.size());
    }

    @Test
    public void test_shouldFilterAvaliacoesByAutor() throws Exception {

        postAvaliacao("Java Basico");
        postAvaliacao("Python Avancado");

        // chamada
        MvcResult result = mockMvc.perform(get("/avaliacao").param("autor", "java"))
                .andExpect(status().isOk())
                .andReturn();

        // asserts
        List<Avaliacao> avaliacoes = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Avaliacao.class));

        Assertions.assertEquals(1, avaliacoes.size());
        Assertions.assertEquals("Java Basico", avaliacoes.get(0).getAutor());
    }

    // ---------- GET /avaliacao/{id} ----------

    @Test
    public void test_shouldFindAvaliacaoById() throws Exception {

        Avaliacao criada = postAvaliacao("Java Basico");

        // chamada
        MvcResult result = mockMvc.perform(get("/avaliacao/" + criada.getId()))
                .andExpect(status().isOk())
                .andReturn();

        // asserts
        Avaliacao avaliacao = objectMapper.readValue(result.getResponse().getContentAsString(), Avaliacao.class);
        Assertions.assertEquals(criada.getId(), avaliacao.getId());
        Assertions.assertEquals("Java Basico", avaliacao.getAutor());
    }

    @Test
    public void test_shouldReturn404WhenAvaliacaoNotFound() throws Exception {

        mockMvc.perform(get("/avaliacao/9999"))
                .andExpect(status().isNotFound());
    }

    // ---------- DELETE /avaliacao/{id} ----------

    @Test
    public void test_shouldDeleteAvaliacao() throws Exception {

        Avaliacao criada = postAvaliacao("Java Basico");

        // chamada
        mockMvc.perform(delete("/avaliacao/" + criada.getId()))
                .andExpect(status().isNoContent());

        // asserts
        mockMvc.perform(get("/avaliacao/" + criada.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void test_shouldReturn404WhenDeleteNotFound() throws Exception {

        mockMvc.perform(delete("/avaliacao/9999"))
                .andExpect(status().isNotFound());
    }

}

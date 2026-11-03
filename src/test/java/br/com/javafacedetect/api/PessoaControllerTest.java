package br.com.javafacedetect.api;

import br.com.javafacedetect.service.ModeloFacialService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PessoaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModeloFacialService modeloFacialService;

    @BeforeEach
    void resetarModelo() {
        modeloFacialService.retreinar();
    }

    @Test
    void deveCadastrarPessoaERetornar201ComId() throws Exception {
        mockMvc.perform(multipart("/api/pessoas")
                        .file(arquivo("imagens", "images/rosto_unico.jpg"))
                        .param("nome", "Maria Silva"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }

    @Test
    void deveBuscarERemoverPessoaCadastrada() throws Exception {
        String resposta = mockMvc.perform(multipart("/api/pessoas")
                        .file(arquivo("imagens", "images/rosto_unico.jpg"))
                        .param("nome", "Joao Souza"))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(resposta).get("id").asLong();

        mockMvc.perform(get("/api/pessoas/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Joao Souza"));

        mockMvc.perform(delete("/api/pessoas/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/pessoas/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PESSOA_NAO_ENCONTRADA"));
    }

    @Test
    void deveRetornar404AoBuscarPessoaInexistente() throws Exception {
        mockMvc.perform(get("/api/pessoas/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PESSOA_NAO_ENCONTRADA"));
    }

    @Test
    void deveRetornar404AoRemoverPessoaInexistente() throws Exception {
        mockMvc.perform(delete("/api/pessoas/{id}", 999999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("PESSOA_NAO_ENCONTRADA"));
    }

    @Test
    void deveRetornar422QuandoImagemSemRostoDetectavel() throws Exception {
        mockMvc.perform(multipart("/api/pessoas")
                        .file(arquivo("imagens", "images/sem_rosto.jpg"))
                        .param("nome", "Alguem"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ROSTO_NAO_DETECTADO"));
    }

    @Test
    void deveRetornar422QuandoImagemComMultiplosRostos() throws Exception {
        mockMvc.perform(multipart("/api/pessoas")
                        .file(arquivo("imagens", "images/multiplos_rostos.jpg"))
                        .param("nome", "Alguem"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("MULTIPLOS_ROSTOS_DETECTADOS"));
    }

    @Test
    void deveRetornar400QuandoFormatoDeArquivoInvalido() throws Exception {
        mockMvc.perform(multipart("/api/pessoas")
                        .file(arquivo("imagens", "images/arquivo_invalido.txt"))
                        .param("nome", "Alguem"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("FORMATO_IMAGEM_INVALIDO"));
    }

    private MockMultipartFile arquivo(String nomeParametro, String recurso) throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(recurso)) {
            byte[] bytes = Objects.requireNonNull(in, "Recurso de teste não encontrado: " + recurso).readAllBytes();
            return new MockMultipartFile(nomeParametro, recurso, "image/jpeg", bytes);
        }
    }
}

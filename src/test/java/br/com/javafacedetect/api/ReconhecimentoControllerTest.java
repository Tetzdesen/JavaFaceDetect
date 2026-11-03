package br.com.javafacedetect.api;

import br.com.javafacedetect.service.ModeloFacialService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReconhecimentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ModeloFacialService modeloFacialService;

    @BeforeEach
    void resetarModelo() {
        modeloFacialService.retreinar();
    }

    @Test
    void deveRetornarNaoReconhecidoQuandoNaoHaPessoaCadastrada() throws Exception {
        mockMvc.perform(multipart("/api/reconhecimento")
                        .file(arquivo("imagem", "images/rosto_unico.jpg")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reconhecido").value(false))
                .andExpect(jsonPath("$.pessoaId").doesNotExist());
    }

    @Test
    void deveReconhecerPessoaCadastradaPreviamente() throws Exception {
        mockMvc.perform(multipart("/api/pessoas")
                        .file(arquivo("imagens", "images/rosto_unico.jpg"))
                        .param("nome", "Maria Silva"))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/api/reconhecimento")
                        .file(arquivo("imagem", "images/rosto_unico.jpg")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reconhecido").value(true))
                .andExpect(jsonPath("$.nome").value("Maria Silva"));
    }

    @Test
    void deveDistinguirDuasPessoasCadastradasPassandoPelaDeteccao() throws Exception {
        mockMvc.perform(multipart("/api/pessoas")
                        .file(arquivo("imagens", "images/pessoa_a_cadastro.png"))
                        .param("nome", "Pessoa A"))
                .andExpect(status().isCreated());
        mockMvc.perform(multipart("/api/pessoas")
                        .file(arquivo("imagens", "images/pessoa_b_cadastro.png"))
                        .param("nome", "Pessoa B"))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/api/reconhecimento")
                        .file(arquivo("imagem", "images/pessoa_a_consulta.png")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reconhecido").value(true))
                .andExpect(jsonPath("$.nome").value("Pessoa A"));
        mockMvc.perform(multipart("/api/reconhecimento")
                        .file(arquivo("imagem", "images/pessoa_b_consulta.png")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reconhecido").value(true))
                .andExpect(jsonPath("$.nome").value("Pessoa B"));
    }

    @Test
    void deveRetornar422QuandoImagemSemRostoDetectavel() throws Exception {
        mockMvc.perform(multipart("/api/reconhecimento")
                        .file(arquivo("imagem", "images/sem_rosto.jpg")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("ROSTO_NAO_DETECTADO"));
    }

    @Test
    void deveRetornar422QuandoImagemComMultiplosRostos() throws Exception {
        mockMvc.perform(multipart("/api/reconhecimento")
                        .file(arquivo("imagem", "images/multiplos_rostos.jpg")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.codigo").value("MULTIPLOS_ROSTOS_DETECTADOS"));
    }

    @Test
    void deveRetornar400QuandoFormatoDeArquivoInvalido() throws Exception {
        mockMvc.perform(multipart("/api/reconhecimento")
                        .file(arquivo("imagem", "images/arquivo_invalido.txt")))
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

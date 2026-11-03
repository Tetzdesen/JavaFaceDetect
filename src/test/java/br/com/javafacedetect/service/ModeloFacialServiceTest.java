package br.com.javafacedetect.service;

import br.com.javafacedetect.domain.Pessoa;
import br.com.javafacedetect.domain.RostoRegistro;
import br.com.javafacedetect.repository.RostoRegistroRepository;
import br.com.javafacedetect.vision.FaceImageUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModeloFacialServiceTest {

    private static final double LIMIAR_CONFIANCA_ALTO = 1000.0;
    private static final double LIMIAR_CONFIANCA_BAIXO = 0.1;

    @Mock
    private RostoRegistroRepository rostoRegistroRepository;

    @Test
    void deveReconhecerPessoaCadastradaAposRetreinar() {
        Pessoa pessoaA = new Pessoa("Pessoa A");
        ReflectionTestUtils.setField(pessoaA, "id", 1L);
        Pessoa pessoaB = new Pessoa("Pessoa B");
        ReflectionTestUtils.setField(pessoaB, "id", 2L);

        RostoRegistro registroA = new RostoRegistro(recortarComoPng("images/pessoa_a_cadastro.png"));
        ReflectionTestUtils.setField(registroA, "pessoa", pessoaA);
        RostoRegistro registroB = new RostoRegistro(recortarComoPng("images/pessoa_b_cadastro.png"));
        ReflectionTestUtils.setField(registroB, "pessoa", pessoaB);

        when(rostoRegistroRepository.findAll()).thenReturn(List.of(registroA, registroB));

        ModeloFacialService service = new ModeloFacialService(rostoRegistroRepository, LIMIAR_CONFIANCA_ALTO);
        service.retreinar();

        Mat consultaA = carregarPng("images/pessoa_a_consulta.png");
        ModeloFacialService.ResultadoReconhecimento resultado = service.reconhecer(consultaA).orElseThrow();

        assertThat(resultado.pessoaId()).isEqualTo(1L);
    }

    @Test
    void deveRetornarVazioQuandoNaoHaPessoaCadastrada() {
        when(rostoRegistroRepository.findAll()).thenReturn(List.of());

        ModeloFacialService service = new ModeloFacialService(rostoRegistroRepository, LIMIAR_CONFIANCA_ALTO);
        service.retreinar();

        Mat consultaA = carregarPng("images/pessoa_a_consulta.png");
        assertThat(service.reconhecer(consultaA)).isEmpty();
    }

    @Test
    void deveRetornarVazioQuandoDistanciaExcedeLimiarConfigurado() {
        Pessoa pessoaA = new Pessoa("Pessoa A");
        ReflectionTestUtils.setField(pessoaA, "id", 1L);
        RostoRegistro registroA = new RostoRegistro(recortarComoPng("images/pessoa_a_cadastro.png"));
        ReflectionTestUtils.setField(registroA, "pessoa", pessoaA);

        when(rostoRegistroRepository.findAll()).thenReturn(List.of(registroA));

        ModeloFacialService service = new ModeloFacialService(rostoRegistroRepository, LIMIAR_CONFIANCA_BAIXO);
        service.retreinar();

        Mat consultaA = carregarPng("images/pessoa_a_consulta.png");
        assertThat(service.reconhecer(consultaA)).isEmpty();
    }

    @Test
    void deveAtualizarModeloAposNovoRetreinoRemovendoRegistro() {
        Pessoa pessoaA = new Pessoa("Pessoa A");
        ReflectionTestUtils.setField(pessoaA, "id", 1L);
        RostoRegistro registroA = new RostoRegistro(recortarComoPng("images/pessoa_a_cadastro.png"));
        ReflectionTestUtils.setField(registroA, "pessoa", pessoaA);

        when(rostoRegistroRepository.findAll())
                .thenReturn(List.of(registroA))
                .thenReturn(List.of());

        ModeloFacialService service = new ModeloFacialService(rostoRegistroRepository, LIMIAR_CONFIANCA_ALTO);
        service.retreinar();
        service.retreinar();

        Mat consultaA = carregarPng("images/pessoa_a_consulta.png");
        assertThat(service.reconhecer(consultaA)).isEmpty();
    }

    private byte[] recortarComoPng(String nomeRecurso) {
        // Os fixtures pessoa_?_cadastro.png já são recortes de rosto em escala de cinza
        // (mesmo formato gravado por CadastroFacialService), então apenas re-encodamos como PNG.
        Mat imagemCinza = carregarPng(nomeRecurso);
        return FaceImageUtils.paraPng(imagemCinza);
    }

    private Mat carregarPng(String nomeRecurso) {
        String caminho = Objects.requireNonNull(getClass().getClassLoader().getResource(nomeRecurso),
                        "Recurso de teste não encontrado: " + nomeRecurso)
                .getPath();
        return imread(caminho, IMREAD_GRAYSCALE);
    }
}

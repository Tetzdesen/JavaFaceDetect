package br.com.javafacedetect.vision;

import org.bytedeco.opencv.opencv_core.Mat;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

class FaceRecognitionModelTest {

    private static final int ROTULO_PESSOA_A = 1;
    private static final int ROTULO_PESSOA_B = 2;

    @Test
    void deveReconhecerCorretamenteCadaPessoaTreinadaComDuasIdentidades() {
        Mat cadastroA = carregarRosto("images/pessoa_a_cadastro.png");
        Mat cadastroB = carregarRosto("images/pessoa_b_cadastro.png");
        Mat consultaA = carregarRosto("images/pessoa_a_consulta.png");
        Mat consultaB = carregarRosto("images/pessoa_b_consulta.png");

        FaceRecognitionModel modelo = new FaceRecognitionModel();
        modelo.treinar(List.of(cadastroA, cadastroB), List.of(ROTULO_PESSOA_A, ROTULO_PESSOA_B));

        FaceRecognitionModel.Resultado resultadoA = modelo.reconhecer(consultaA).orElseThrow();
        FaceRecognitionModel.Resultado resultadoB = modelo.reconhecer(consultaB).orElseThrow();

        assertThat(resultadoA.rotulo()).isEqualTo(ROTULO_PESSOA_A);
        assertThat(resultadoB.rotulo()).isEqualTo(ROTULO_PESSOA_B);
    }

    @Test
    void deveRetornarVazioQuandoModeloNaoFoiTreinado() {
        FaceRecognitionModel modelo = new FaceRecognitionModel();
        Mat consulta = carregarRosto("images/pessoa_a_consulta.png");

        assertThat(modelo.reconhecer(consulta)).isEmpty();
        assertThat(modelo.isTreinado()).isFalse();
    }

    private Mat carregarRosto(String nomeRecurso) {
        String caminho = Objects.requireNonNull(getClass().getClassLoader().getResource(nomeRecurso),
                        "Recurso de teste não encontrado: " + nomeRecurso)
                .getPath();
        return imread(caminho, IMREAD_GRAYSCALE);
    }
}

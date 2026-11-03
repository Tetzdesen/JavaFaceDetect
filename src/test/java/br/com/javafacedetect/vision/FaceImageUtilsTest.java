package br.com.javafacedetect.vision;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

class FaceImageUtilsTest {

    private final FaceDetector detector = new FaceDetector();

    @Test
    void deveRecortarERedimensionarRostoParaTamanhoPadraoEmEscalaDeCinza() {
        Mat imagem = imread(caminhoRecurso("images/rosto_unico.jpg"));
        Rect rosto = detector.detectar(imagem).get(0);

        Mat resultado = FaceImageUtils.recortarERedimensionar(imagem, rosto);

        assertThat(resultado.cols()).isEqualTo(FaceImageUtils.TAMANHO_PADRAO_PX);
        assertThat(resultado.rows()).isEqualTo(FaceImageUtils.TAMANHO_PADRAO_PX);
        assertThat(resultado.channels()).isEqualTo(1);
    }

    @Test
    void deveCodificarEDecodificarImagemSemPerderDimensoes() throws IOException {
        Mat imagem = imread(caminhoRecurso("images/rosto_unico.jpg"));
        Rect rosto = detector.detectar(imagem).get(0);
        Mat recortado = FaceImageUtils.recortarERedimensionar(imagem, rosto);

        byte[] png = FaceImageUtils.paraPng(recortado);
        Mat decodificado = FaceImageUtils.decodificar(png);

        assertThat(decodificado).isNotNull();
        assertThat(decodificado.cols()).isEqualTo(FaceImageUtils.TAMANHO_PADRAO_PX);
        assertThat(decodificado.rows()).isEqualTo(FaceImageUtils.TAMANHO_PADRAO_PX);
    }

    @Test
    void deveRetornarNuloAoDecodificarBytesInvalidos() throws IOException {
        byte[] invalido = Files.readAllBytes(java.nio.file.Path.of(caminhoRecurso("images/arquivo_invalido.txt")));

        Mat resultado = FaceImageUtils.decodificar(invalido);

        assertThat(resultado).isNull();
    }

    private String caminhoRecurso(String nome) {
        return Objects.requireNonNull(getClass().getClassLoader().getResource(nome),
                        "Recurso de teste não encontrado: " + nome)
                .getPath();
    }
}

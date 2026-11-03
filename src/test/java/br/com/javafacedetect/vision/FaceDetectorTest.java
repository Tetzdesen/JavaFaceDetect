package br.com.javafacedetect.vision;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;

class FaceDetectorTest {

    private final FaceDetector detector = new FaceDetector();

    @Test
    void deveDetectarUmRostoEmImagemComUmRostoUnico() {
        Mat imagem = imread(caminhoRecurso("images/rosto_unico.jpg"));

        List<Rect> rostos = detector.detectar(imagem);

        assertThat(rostos).hasSize(1);
    }

    @Test
    void deveRetornarListaVaziaQuandoImagemNaoTemRosto() {
        Mat imagem = imread(caminhoRecurso("images/sem_rosto.jpg"));

        List<Rect> rostos = detector.detectar(imagem);

        assertThat(rostos).isEmpty();
    }

    @Test
    void deveDetectarMaisDeUmRostoEmImagemComMultiplosRostos() {
        Mat imagem = imread(caminhoRecurso("images/multiplos_rostos.jpg"));

        List<Rect> rostos = detector.detectar(imagem);

        assertThat(rostos.size()).isGreaterThanOrEqualTo(2);
    }

    private String caminhoRecurso(String nome) {
        return Objects.requireNonNull(getClass().getClassLoader().getResource(nome),
                        "Recurso de teste não encontrado: " + nome)
                .getPath();
    }
}

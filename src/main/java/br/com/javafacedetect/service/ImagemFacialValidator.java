package br.com.javafacedetect.service;

import br.com.javafacedetect.service.exception.ArquivoMuitoGrandeException;
import br.com.javafacedetect.service.exception.FormatoImagemInvalidoException;
import br.com.javafacedetect.service.exception.MultiplosRostosDetectadosException;
import br.com.javafacedetect.service.exception.RostoNaoDetectadoException;
import br.com.javafacedetect.vision.FaceDetector;
import br.com.javafacedetect.vision.FaceImageUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Validação compartilhada de imagem usada tanto no cadastro quanto no reconhecimento facial:
 * tamanho máximo, formato suportado e presença de exatamente um rosto detectável.
 */
@Component
public class ImagemFacialValidator {

    private final FaceDetector faceDetector;
    private final long tamanhoMaximoBytes;

    public ImagemFacialValidator(FaceDetector faceDetector,
                                  @Value("${face-recognition.max-image-size-bytes}") long tamanhoMaximoBytes) {
        this.faceDetector = faceDetector;
        this.tamanhoMaximoBytes = tamanhoMaximoBytes;
    }

    /**
     * Valida os bytes da imagem e garante que exatamente um rosto seja detectável.
     *
     * @throws ArquivoMuitoGrandeException      se o arquivo exceder o tamanho máximo permitido
     * @throws FormatoImagemInvalidoException   se os bytes não puderem ser decodificados como imagem
     * @throws RostoNaoDetectadoException       se nenhum rosto for detectado
     * @throws MultiplosRostosDetectadosException se mais de um rosto for detectado
     */
    public RostoValidado validarEDetectarRostoUnico(byte[] imagemBytes) {
        if (imagemBytes.length > tamanhoMaximoBytes) {
            throw new ArquivoMuitoGrandeException(imagemBytes.length, tamanhoMaximoBytes);
        }

        Mat imagem = FaceImageUtils.decodificar(imagemBytes);
        if (imagem == null) {
            throw new FormatoImagemInvalidoException();
        }

        List<Rect> rostos = faceDetector.detectar(imagem);
        if (rostos.isEmpty()) {
            throw new RostoNaoDetectadoException();
        }
        if (rostos.size() > 1) {
            throw new MultiplosRostosDetectadosException();
        }

        return new RostoValidado(imagem, rostos.get(0));
    }

    public record RostoValidado(Mat imagem, Rect rosto) {
    }
}

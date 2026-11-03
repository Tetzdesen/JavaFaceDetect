package br.com.javafacedetect.vision;

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.equalizeHist;

/**
 * Wrapper sobre o CascadeClassifier (Haar Cascade) do OpenCV/JavaCV para localizar
 * regiões de rosto em uma imagem.
 */
@Component
public class FaceDetector {

    private static final String CASCADE_RESOURCE = "/cascades/haarcascade_frontalface_default.xml";
    private static final double SCALE_FACTOR = 1.1;
    private static final int MIN_NEIGHBORS = 4;
    private static final int MIN_FACE_SIZE_PX = 30;

    private final CascadeClassifier cascadeClassifier;

    public FaceDetector() {
        File cascadeFile = extractCascadeResource();
        this.cascadeClassifier = new CascadeClassifier(cascadeFile.getAbsolutePath());
        if (cascadeClassifier.empty()) {
            throw new IllegalStateException("Não foi possível carregar o classificador Haar Cascade de detecção facial");
        }
    }

    /**
     * Detecta rostos em uma imagem (BGR ou escala de cinza).
     *
     * @param imagem imagem decodificada via OpenCV
     * @return lista de retângulos correspondentes aos rostos detectados (pode ser vazia)
     */
    public List<Rect> detectar(Mat imagem) {
        Mat cinza = new Mat();
        if (imagem.channels() > 1) {
            cvtColor(imagem, cinza, COLOR_BGR2GRAY);
        } else {
            imagem.copyTo(cinza);
        }
        equalizeHist(cinza, cinza);

        RectVector rostosDetectados = new RectVector();
        cascadeClassifier.detectMultiScale(
                cinza,
                rostosDetectados,
                SCALE_FACTOR,
                MIN_NEIGHBORS,
                0,
                new Size(MIN_FACE_SIZE_PX, MIN_FACE_SIZE_PX),
                new Size());

        List<Rect> resultado = new ArrayList<>();
        for (long i = 0; i < rostosDetectados.size(); i++) {
            resultado.add(new Rect(rostosDetectados.get(i)));
        }
        return resultado;
    }

    private File extractCascadeResource() {
        try {
            File tempFile = File.createTempFile("haarcascade_frontalface_default", ".xml");
            tempFile.deleteOnExit();
            try (InputStream in = getClass().getResourceAsStream(CASCADE_RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException(
                            "Recurso do classificador Haar Cascade não encontrado no classpath: " + CASCADE_RESOURCE);
                }
                Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return tempFile;
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao extrair o classificador Haar Cascade para um arquivo temporário", e);
        }
    }
}

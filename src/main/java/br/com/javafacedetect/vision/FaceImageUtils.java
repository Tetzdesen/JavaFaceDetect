package br.com.javafacedetect.vision;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Size;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_UNCHANGED;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imdecode;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imencode;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;

/**
 * Utilitários de recorte, normalização e (de)serialização de imagens de rosto,
 * usados antes de treinar/comparar com o modelo de reconhecimento (LBPH).
 */
public final class FaceImageUtils {

    public static final int TAMANHO_PADRAO_PX = 200;

    private FaceImageUtils() {
    }

    /**
     * Recorta a região do rosto na imagem original, converte para escala de cinza
     * e redimensiona para um tamanho padrão, para uso consistente no treino/comparação LBPH.
     */
    public static Mat recortarERedimensionar(Mat imagemOriginal, Rect rosto) {
        Mat cinza = new Mat();
        if (imagemOriginal.channels() > 1) {
            cvtColor(imagemOriginal, cinza, COLOR_BGR2GRAY);
        } else {
            imagemOriginal.copyTo(cinza);
        }

        Mat recorte = new Mat(cinza, rosto);
        Mat redimensionado = new Mat();
        resize(recorte, redimensionado, new Size(TAMANHO_PADRAO_PX, TAMANHO_PADRAO_PX));
        return redimensionado;
    }

    /** Codifica uma imagem OpenCV como PNG em memória, para persistência. */
    public static byte[] paraPng(Mat imagem) {
        BytePointer buffer = new BytePointer();
        imencode(".png", imagem, buffer);
        byte[] dados = new byte[(int) buffer.limit()];
        buffer.get(dados);
        return dados;
    }

    /** Decodifica bytes de uma imagem (PNG/JPEG) para uma matriz OpenCV, ou {@code null} se inválida. */
    public static Mat decodificar(byte[] bytes) {
        Mat bufferBytes = new Mat(1, bytes.length, CV_8UC1, new BytePointer(bytes));
        Mat imagem = imdecode(bufferBytes, IMREAD_UNCHANGED);
        return imagem.empty() ? null : imagem;
    }
}

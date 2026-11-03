package br.com.javafacedetect.vision;

import org.bytedeco.javacpp.indexer.IntIndexer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;

import java.util.List;
import java.util.Optional;

import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;

/**
 * Wrapper sobre o {@link LBPHFaceRecognizer} do OpenCV/JavaCV: treina um modelo a partir
 * de imagens de rosto (já recortadas/normalizadas) associadas a um rótulo inteiro, e
 * compara uma imagem de entrada contra o modelo treinado.
 *
 * <p>O valor de distância (confidence) retornado pelo LBPH é uma <b>distância</b>: quanto
 * <b>menor</b>, melhor a correspondência (0 = correspondência perfeita).</p>
 */
public class FaceRecognitionModel {

    private final LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
    private boolean treinado = false;

    /**
     * Treina (ou retreina) o modelo do zero com as imagens e rótulos fornecidos.
     * Chamar novamente substitui completamente o treino anterior.
     */
    public void treinar(List<Mat> imagens, List<Integer> rotulos) {
        if (imagens.isEmpty() || imagens.size() != rotulos.size()) {
            this.treinado = false;
            return;
        }

        MatVector matVector = new MatVector(imagens.size());
        for (int i = 0; i < imagens.size(); i++) {
            matVector.put(i, imagens.get(i));
        }

        Mat matrizRotulos = new Mat(rotulos.size(), 1, CV_32SC1);
        IntIndexer indexer = matrizRotulos.createIndexer();
        for (int i = 0; i < rotulos.size(); i++) {
            indexer.put(i, 0, rotulos.get(i));
        }

        recognizer.train(matVector, matrizRotulos);
        this.treinado = true;
    }

    /**
     * Compara a imagem de rosto informada contra o modelo treinado.
     *
     * @return o rótulo mais próximo e a distância correspondente, ou vazio se o modelo
     *         ainda não foi treinado (nenhuma pessoa cadastrada)
     */
    public Optional<Resultado> reconhecer(Mat imagemRosto) {
        if (!treinado) {
            return Optional.empty();
        }
        int[] rotulo = new int[1];
        double[] distancia = new double[1];
        recognizer.predict(imagemRosto, rotulo, distancia);
        return Optional.of(new Resultado(rotulo[0], distancia[0]));
    }

    public boolean isTreinado() {
        return treinado;
    }

    public record Resultado(int rotulo, double distancia) {
    }
}

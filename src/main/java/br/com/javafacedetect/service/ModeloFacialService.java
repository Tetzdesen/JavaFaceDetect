package br.com.javafacedetect.service;

import br.com.javafacedetect.domain.RostoRegistro;
import br.com.javafacedetect.repository.RostoRegistroRepository;
import br.com.javafacedetect.vision.FaceImageUtils;
import br.com.javafacedetect.vision.FaceRecognitionModel;
import jakarta.annotation.PostConstruct;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Mantém o modelo de reconhecimento facial (LBPH) treinado em memória, retreinando-o
 * a partir de todos os registros faciais persistidos sempre que a base de cadastros muda.
 * Aplica o limiar mínimo de confiança configurado ao interpretar o resultado de uma comparação.
 */
@Service
public class ModeloFacialService {

    private final RostoRegistroRepository rostoRegistroRepository;
    private final double limiarConfianca;
    private final AtomicReference<FaceRecognitionModel> modeloAtual = new AtomicReference<>(new FaceRecognitionModel());

    public ModeloFacialService(RostoRegistroRepository rostoRegistroRepository,
                                @Value("${face-recognition.confidence-threshold}") double limiarConfianca) {
        this.rostoRegistroRepository = rostoRegistroRepository;
        this.limiarConfianca = limiarConfianca;
    }

    @PostConstruct
    public void inicializar() {
        retreinar();
    }

    /** Retreina o modelo completo a partir de todos os registros faciais atualmente persistidos. */
    public synchronized void retreinar() {
        List<RostoRegistro> registros = rostoRegistroRepository.findAll();

        List<Mat> imagens = new ArrayList<>();
        List<Integer> rotulos = new ArrayList<>();
        for (RostoRegistro registro : registros) {
            Mat imagem = FaceImageUtils.decodificar(registro.getImagemRecortada());
            if (imagem != null) {
                imagens.add(imagem);
                rotulos.add(Math.toIntExact(registro.getPessoa().getId()));
            }
        }

        FaceRecognitionModel novoModelo = new FaceRecognitionModel();
        novoModelo.treinar(imagens, rotulos);
        modeloAtual.set(novoModelo);
    }

    /**
     * Compara a imagem de rosto informada contra o modelo treinado, aplicando o limiar de confiança.
     *
     * @return o id da pessoa reconhecida e a distância da correspondência, ou vazio se o modelo não
     *         está treinado (nenhuma pessoa cadastrada) ou se a melhor correspondência está abaixo
     *         do limiar mínimo de confiança configurado
     */
    public Optional<ResultadoReconhecimento> reconhecer(Mat imagemRosto) {
        FaceRecognitionModel modelo = modeloAtual.get();
        if (!modelo.isTreinado()) {
            return Optional.empty();
        }
        return modelo.reconhecer(imagemRosto)
                .filter(resultado -> resultado.distancia() <= limiarConfianca)
                .map(resultado -> new ResultadoReconhecimento((long) resultado.rotulo(), resultado.distancia()));
    }

    public record ResultadoReconhecimento(Long pessoaId, double distancia) {
    }
}

package br.com.javafacedetect.service;

import br.com.javafacedetect.domain.Pessoa;
import br.com.javafacedetect.repository.PessoaRepository;
import br.com.javafacedetect.vision.FaceImageUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Reconhecimento facial a partir de uma imagem de entrada (capability face-recognition):
 * valida a imagem, detecta o rosto, compara contra o modelo treinado e resolve a pessoa
 * correspondente quando a confiança está dentro do limiar configurado.
 */
@Service
public class ReconhecimentoFacialService {

    private final ImagemFacialValidator imagemFacialValidator;
    private final ModeloFacialService modeloFacialService;
    private final PessoaRepository pessoaRepository;

    public ReconhecimentoFacialService(ImagemFacialValidator imagemFacialValidator,
                                        ModeloFacialService modeloFacialService,
                                        PessoaRepository pessoaRepository) {
        this.imagemFacialValidator = imagemFacialValidator;
        this.modeloFacialService = modeloFacialService;
        this.pessoaRepository = pessoaRepository;
    }

    /**
     * Reconhece o rosto presente na imagem informada.
     *
     * @return a pessoa reconhecida e a distância da correspondência, ou vazio quando o rosto
     *         não corresponde a nenhuma pessoa cadastrada com confiança suficiente (inclui o
     *         caso de não haver nenhuma pessoa cadastrada)
     */
    @Transactional(readOnly = true)
    public Optional<ResultadoReconhecimento> reconhecer(byte[] imagemBytes) {
        ImagemFacialValidator.RostoValidado validado = imagemFacialValidator.validarEDetectarRostoUnico(imagemBytes);
        Mat rostoRecortado = FaceImageUtils.recortarERedimensionar(validado.imagem(), validado.rosto());

        return modeloFacialService.reconhecer(rostoRecortado)
                .map(resultado -> {
                    Pessoa pessoa = pessoaRepository.findById(resultado.pessoaId())
                            .orElseThrow(() -> new IllegalStateException(
                                    "Modelo apontou para pessoa inexistente id=" + resultado.pessoaId()));
                    return new ResultadoReconhecimento(pessoa.getId(), pessoa.getNome(), resultado.distancia());
                });
    }

    public record ResultadoReconhecimento(Long pessoaId, String nome, double distancia) {
    }
}

package br.com.javafacedetect.service;

import br.com.javafacedetect.domain.Pessoa;
import br.com.javafacedetect.domain.RostoRegistro;
import br.com.javafacedetect.repository.PessoaRepository;
import br.com.javafacedetect.service.exception.PessoaNaoEncontradaException;
import br.com.javafacedetect.vision.FaceImageUtils;
import org.bytedeco.opencv.opencv_core.Mat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Cadastro de pessoas com fotos de rosto (capability face-enrollment): valida cada imagem,
 * extrai e persiste os dados faciais, e mantém o modelo de reconhecimento atualizado.
 */
@Service
public class CadastroFacialService {

    private final PessoaRepository pessoaRepository;
    private final ImagemFacialValidator imagemFacialValidator;
    private final ModeloFacialService modeloFacialService;

    public CadastroFacialService(PessoaRepository pessoaRepository,
                                  ImagemFacialValidator imagemFacialValidator,
                                  ModeloFacialService modeloFacialService) {
        this.pessoaRepository = pessoaRepository;
        this.imagemFacialValidator = imagemFacialValidator;
        this.modeloFacialService = modeloFacialService;
    }

    /**
     * Cadastra uma nova pessoa a partir do nome e de uma ou mais imagens de rosto.
     * Cada imagem deve conter exatamente um rosto detectável.
     */
    @Transactional
    public Pessoa cadastrar(String nome, List<byte[]> imagens) {
        if (imagens == null || imagens.isEmpty()) {
            throw new IllegalArgumentException("Ao menos uma imagem de rosto é obrigatória para o cadastro");
        }

        List<byte[]> rostosRecortadosPng = new ArrayList<>();
        for (byte[] imagemBytes : imagens) {
            ImagemFacialValidator.RostoValidado validado = imagemFacialValidator.validarEDetectarRostoUnico(imagemBytes);
            Mat rostoRecortado = FaceImageUtils.recortarERedimensionar(validado.imagem(), validado.rosto());
            rostosRecortadosPng.add(FaceImageUtils.paraPng(rostoRecortado));
        }

        Pessoa pessoa = new Pessoa(nome);
        for (byte[] rostoPng : rostosRecortadosPng) {
            pessoa.adicionarRosto(new RostoRegistro(rostoPng));
        }

        Pessoa salva = pessoaRepository.save(pessoa);
        modeloFacialService.retreinar();
        return salva;
    }

    @Transactional(readOnly = true)
    public Pessoa buscarPorId(Long id) {
        return pessoaRepository.findById(id)
                .orElseThrow(() -> new PessoaNaoEncontradaException(id));
    }

    @Transactional
    public void remover(Long id) {
        if (!pessoaRepository.existsById(id)) {
            throw new PessoaNaoEncontradaException(id);
        }
        pessoaRepository.deleteById(id);
        modeloFacialService.retreinar();
    }
}

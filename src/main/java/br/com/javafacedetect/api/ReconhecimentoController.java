package br.com.javafacedetect.api;

import br.com.javafacedetect.api.dto.ReconhecimentoResponse;
import br.com.javafacedetect.service.ReconhecimentoFacialService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@RestController
@RequestMapping("/api/reconhecimento")
public class ReconhecimentoController {

    private final ReconhecimentoFacialService reconhecimentoFacialService;

    public ReconhecimentoController(ReconhecimentoFacialService reconhecimentoFacialService) {
        this.reconhecimentoFacialService = reconhecimentoFacialService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ReconhecimentoResponse reconhecer(@RequestParam("imagem") MultipartFile imagem) {
        return reconhecimentoFacialService.reconhecer(lerBytes(imagem))
                .map(r -> ReconhecimentoResponse.reconhecido(r.pessoaId(), r.nome(), r.distancia()))
                .orElseGet(ReconhecimentoResponse::naoReconhecido);
    }

    private byte[] lerBytes(MultipartFile arquivo) {
        try {
            return arquivo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

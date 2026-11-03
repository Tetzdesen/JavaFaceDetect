package br.com.javafacedetect.api;

import br.com.javafacedetect.api.dto.PessoaResponse;
import br.com.javafacedetect.domain.Pessoa;
import br.com.javafacedetect.service.CadastroFacialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@RestController
@RequestMapping("/api/pessoas")
public class PessoaController {

    private final CadastroFacialService cadastroFacialService;

    public PessoaController(CadastroFacialService cadastroFacialService) {
        this.cadastroFacialService = cadastroFacialService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PessoaResponse> cadastrar(@RequestParam("nome") String nome,
                                                      @RequestParam("imagens") List<MultipartFile> imagens) {
        List<byte[]> bytesImagens = imagens.stream().map(this::lerBytes).toList();
        Pessoa pessoa = cadastroFacialService.cadastrar(nome, bytesImagens);
        return ResponseEntity.status(HttpStatus.CREATED).body(PessoaResponse.de(pessoa));
    }

    @GetMapping("/{id}")
    public PessoaResponse buscar(@PathVariable Long id) {
        return PessoaResponse.de(cadastroFacialService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        cadastroFacialService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private byte[] lerBytes(MultipartFile arquivo) {
        try {
            return arquivo.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

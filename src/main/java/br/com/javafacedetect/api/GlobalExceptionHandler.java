package br.com.javafacedetect.api;

import br.com.javafacedetect.api.dto.ErroResponse;
import br.com.javafacedetect.service.exception.ArquivoMuitoGrandeException;
import br.com.javafacedetect.service.exception.FormatoImagemInvalidoException;
import br.com.javafacedetect.service.exception.MultiplosRostosDetectadosException;
import br.com.javafacedetect.service.exception.PessoaNaoEncontradaException;
import br.com.javafacedetect.service.exception.RostoNaoDetectadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RostoNaoDetectadoException.class)
    public ResponseEntity<ErroResponse> tratarRostoNaoDetectado(RostoNaoDetectadoException e) {
        return corpo(HttpStatus.UNPROCESSABLE_ENTITY, "ROSTO_NAO_DETECTADO", e.getMessage());
    }

    @ExceptionHandler(MultiplosRostosDetectadosException.class)
    public ResponseEntity<ErroResponse> tratarMultiplosRostos(MultiplosRostosDetectadosException e) {
        return corpo(HttpStatus.UNPROCESSABLE_ENTITY, "MULTIPLOS_ROSTOS_DETECTADOS", e.getMessage());
    }

    @ExceptionHandler(FormatoImagemInvalidoException.class)
    public ResponseEntity<ErroResponse> tratarFormatoInvalido(FormatoImagemInvalidoException e) {
        return corpo(HttpStatus.BAD_REQUEST, "FORMATO_IMAGEM_INVALIDO", e.getMessage());
    }

    @ExceptionHandler({ArquivoMuitoGrandeException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ErroResponse> tratarArquivoMuitoGrande(Exception e) {
        return corpo(HttpStatus.PAYLOAD_TOO_LARGE, "ARQUIVO_MUITO_GRANDE", e.getMessage());
    }

    @ExceptionHandler(PessoaNaoEncontradaException.class)
    public ResponseEntity<ErroResponse> tratarPessoaNaoEncontrada(PessoaNaoEncontradaException e) {
        return corpo(HttpStatus.NOT_FOUND, "PESSOA_NAO_ENCONTRADA", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroResponse> tratarRequisicaoInvalida(IllegalArgumentException e) {
        return corpo(HttpStatus.BAD_REQUEST, "REQUISICAO_INVALIDA", e.getMessage());
    }

    private ResponseEntity<ErroResponse> corpo(HttpStatus status, String codigo, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(codigo, mensagem));
    }
}

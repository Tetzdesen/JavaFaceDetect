package br.com.javafacedetect.service.exception;

public class ArquivoMuitoGrandeException extends RuntimeException {

    public ArquivoMuitoGrandeException(long tamanhoBytes, long tamanhoMaximoBytes) {
        super("Arquivo de %d bytes excede o tamanho máximo permitido de %d bytes"
                .formatted(tamanhoBytes, tamanhoMaximoBytes));
    }
}

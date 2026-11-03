package br.com.javafacedetect.service.exception;

public class RostoNaoDetectadoException extends RuntimeException {

    public RostoNaoDetectadoException() {
        super("Nenhum rosto foi detectado na imagem enviada");
    }
}

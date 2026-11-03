package br.com.javafacedetect.service.exception;

public class MultiplosRostosDetectadosException extends RuntimeException {

    public MultiplosRostosDetectadosException() {
        super("A imagem deve conter exatamente um rosto, mas mais de um foi detectado");
    }
}

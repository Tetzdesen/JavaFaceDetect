package br.com.javafacedetect.service.exception;

public class FormatoImagemInvalidoException extends RuntimeException {

    public FormatoImagemInvalidoException() {
        super("O arquivo enviado não é uma imagem em um formato suportado (ex.: JPEG ou PNG)");
    }
}

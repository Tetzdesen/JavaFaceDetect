package br.com.javafacedetect.service.exception;

public class PessoaNaoEncontradaException extends RuntimeException {

    public PessoaNaoEncontradaException(Long id) {
        super("Pessoa não encontrada com id " + id);
    }
}

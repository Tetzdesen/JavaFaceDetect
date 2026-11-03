package br.com.javafacedetect.api.dto;

import br.com.javafacedetect.domain.Pessoa;

import java.time.Instant;

public record PessoaResponse(Long id, String nome, Instant criadoEm) {

    public static PessoaResponse de(Pessoa pessoa) {
        return new PessoaResponse(pessoa.getId(), pessoa.getNome(), pessoa.getCriadoEm());
    }
}

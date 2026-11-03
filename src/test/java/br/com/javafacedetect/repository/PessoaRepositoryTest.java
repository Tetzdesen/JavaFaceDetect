package br.com.javafacedetect.repository;

import br.com.javafacedetect.domain.Pessoa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PessoaRepositoryTest {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Test
    void deveSalvarEBuscarPessoaPorId() {
        Pessoa pessoa = pessoaRepository.save(new Pessoa("Maria Silva"));

        Optional<Pessoa> encontrada = pessoaRepository.findById(pessoa.getId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getNome()).isEqualTo("Maria Silva");
        assertThat(encontrada.get().getCriadoEm()).isNotNull();
    }
}

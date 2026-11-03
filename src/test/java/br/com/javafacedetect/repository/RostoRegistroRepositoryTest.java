package br.com.javafacedetect.repository;

import br.com.javafacedetect.domain.Pessoa;
import br.com.javafacedetect.domain.RostoRegistro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class RostoRegistroRepositoryTest {

    @Autowired
    private PessoaRepository pessoaRepository;

    @Autowired
    private RostoRegistroRepository rostoRegistroRepository;

    @Test
    void devePersistirRegistroFacialVinculadoAPessoa() {
        Pessoa pessoa = pessoaRepository.save(new Pessoa("Joao Souza"));
        pessoa.adicionarRosto(new RostoRegistro(new byte[] {1, 2, 3}));
        pessoaRepository.save(pessoa);

        List<RostoRegistro> registros = rostoRegistroRepository.findByPessoaId(pessoa.getId());

        assertThat(registros).hasSize(1);
        assertThat(registros.get(0).getPessoa().getId()).isEqualTo(pessoa.getId());
        assertThat(registros.get(0).getImagemRecortada()).containsExactly(1, 2, 3);
    }
}

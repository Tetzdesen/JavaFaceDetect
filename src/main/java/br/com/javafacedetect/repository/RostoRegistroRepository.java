package br.com.javafacedetect.repository;

import br.com.javafacedetect.domain.RostoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RostoRegistroRepository extends JpaRepository<RostoRegistro, Long> {

    List<RostoRegistro> findByPessoaId(Long pessoaId);
}

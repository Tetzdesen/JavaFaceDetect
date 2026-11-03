package br.com.javafacedetect.service;

import br.com.javafacedetect.domain.Pessoa;
import br.com.javafacedetect.repository.PessoaRepository;
import br.com.javafacedetect.service.exception.RostoNaoDetectadoException;
import br.com.javafacedetect.vision.FaceDetector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReconhecimentoFacialServiceTest {

    private static final long TAMANHO_MAXIMO_BYTES = 5_000_000L;

    @Mock
    private ModeloFacialService modeloFacialService;

    @Mock
    private PessoaRepository pessoaRepository;

    private final ImagemFacialValidator validator = new ImagemFacialValidator(new FaceDetector(), TAMANHO_MAXIMO_BYTES);

    private ReconhecimentoFacialService service() {
        return new ReconhecimentoFacialService(validator, modeloFacialService, pessoaRepository);
    }

    @Test
    void deveReconhecerPessoaCadastradaComBoaCorrespondencia() {
        Pessoa pessoa = new Pessoa("Maria Silva");
        ReflectionTestUtils.setField(pessoa, "id", 7L);

        lenient().when(modeloFacialService.reconhecer(any()))
                .thenReturn(Optional.of(new ModeloFacialService.ResultadoReconhecimento(7L, 35.0)));
        when(pessoaRepository.findById(7L)).thenReturn(Optional.of(pessoa));

        Optional<ReconhecimentoFacialService.ResultadoReconhecimento> resultado =
                service().reconhecer(bytesRecurso("images/rosto_unico.jpg"));

        assertThat(resultado).isPresent();
        assertThat(resultado.get().pessoaId()).isEqualTo(7L);
        assertThat(resultado.get().nome()).isEqualTo("Maria Silva");
        assertThat(resultado.get().distancia()).isEqualTo(35.0);
    }

    @Test
    void deveRetornarVazioQuandoRostoNaoCorrespondeANinguemCadastrado() {
        lenient().when(modeloFacialService.reconhecer(any())).thenReturn(Optional.empty());

        Optional<ReconhecimentoFacialService.ResultadoReconhecimento> resultado =
                service().reconhecer(bytesRecurso("images/rosto_unico.jpg"));

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveRetornarVazioQuandoNaoHaNenhumaPessoaCadastrada() {
        lenient().when(modeloFacialService.reconhecer(any())).thenReturn(Optional.empty());

        Optional<ReconhecimentoFacialService.ResultadoReconhecimento> resultado =
                service().reconhecer(bytesRecurso("images/rosto_unico.jpg"));

        assertThat(resultado).isEmpty();
    }

    @Test
    void deveRejeitarImagemSemRostoDetectavel() {
        assertThatThrownBy(() -> service().reconhecer(bytesRecurso("images/sem_rosto.jpg")))
                .isInstanceOf(RostoNaoDetectadoException.class);
    }

    private byte[] bytesRecurso(String nomeRecurso) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(nomeRecurso)) {
            return Objects.requireNonNull(in, "Recurso de teste não encontrado: " + nomeRecurso).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

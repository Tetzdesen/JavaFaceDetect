package br.com.javafacedetect.service;

import br.com.javafacedetect.domain.Pessoa;
import br.com.javafacedetect.repository.PessoaRepository;
import br.com.javafacedetect.service.exception.ArquivoMuitoGrandeException;
import br.com.javafacedetect.service.exception.FormatoImagemInvalidoException;
import br.com.javafacedetect.service.exception.MultiplosRostosDetectadosException;
import br.com.javafacedetect.service.exception.PessoaNaoEncontradaException;
import br.com.javafacedetect.service.exception.RostoNaoDetectadoException;
import br.com.javafacedetect.vision.FaceDetector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastroFacialServiceTest {

    private static final long TAMANHO_MAXIMO_BYTES = 5_000_000L;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private ModeloFacialService modeloFacialService;

    private final ImagemFacialValidator validator = new ImagemFacialValidator(new FaceDetector(), TAMANHO_MAXIMO_BYTES);

    private CadastroFacialService service() {
        return new CadastroFacialService(pessoaRepository, validator, modeloFacialService);
    }

    private void stubSalvarComIdGerado() {
        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(invocation -> {
            Pessoa pessoa = invocation.getArgument(0);
            ReflectionTestUtils.setField(pessoa, "id", 42L);
            return pessoa;
        });
    }

    @Test
    void deveCadastrarPessoaComUmaFoto() {
        stubSalvarComIdGerado();
        lenient().doNothing().when(modeloFacialService).retreinar();

        Pessoa pessoa = service().cadastrar("Maria Silva", List.of(bytesRecurso("images/rosto_unico.jpg")));

        assertThat(pessoa.getId()).isEqualTo(42L);
        assertThat(pessoa.getNome()).isEqualTo("Maria Silva");
        assertThat(pessoa.getRostos()).hasSize(1);
        verify(modeloFacialService, times(1)).retreinar();
    }

    @Test
    void deveCadastrarPessoaComMultiplasFotos() {
        stubSalvarComIdGerado();
        lenient().doNothing().when(modeloFacialService).retreinar();

        Pessoa pessoa = service().cadastrar("Joao Souza",
                List.of(bytesRecurso("images/rosto_unico.jpg"), bytesRecurso("images/rosto_unico.jpg")));

        assertThat(pessoa.getRostos()).hasSize(2);
        verify(modeloFacialService, times(1)).retreinar();
    }

    @Test
    void deveRejeitarImagemSemRostoDetectavel() {
        assertThatThrownBy(() -> service().cadastrar("Alguem", List.of(bytesRecurso("images/sem_rosto.jpg"))))
                .isInstanceOf(RostoNaoDetectadoException.class);
        verify(pessoaRepository, never()).save(any());
        verify(modeloFacialService, never()).retreinar();
    }

    @Test
    void deveRejeitarImagemComMultiplosRostos() {
        assertThatThrownBy(() -> service().cadastrar("Alguem", List.of(bytesRecurso("images/multiplos_rostos.jpg"))))
                .isInstanceOf(MultiplosRostosDetectadosException.class);
        verify(pessoaRepository, never()).save(any());
    }

    @Test
    void deveRejeitarFormatoDeArquivoInvalido() {
        assertThatThrownBy(() -> service().cadastrar("Alguem", List.of(bytesRecurso("images/arquivo_invalido.txt"))))
                .isInstanceOf(FormatoImagemInvalidoException.class);
        verify(pessoaRepository, never()).save(any());
    }

    @Test
    void deveRejeitarArquivoAcimaDoTamanhoMaximo() {
        byte[] arquivoGrande = new byte[(int) TAMANHO_MAXIMO_BYTES + 1];

        assertThatThrownBy(() -> service().cadastrar("Alguem", List.of(arquivoGrande)))
                .isInstanceOf(ArquivoMuitoGrandeException.class);
        verify(pessoaRepository, never()).save(any());
    }

    @Test
    void deveBuscarPessoaCadastradaPorId() {
        Pessoa pessoa = new Pessoa("Maria Silva");
        ReflectionTestUtils.setField(pessoa, "id", 1L);
        when(pessoaRepository.findById(1L)).thenReturn(Optional.of(pessoa));

        Pessoa encontrada = service().buscarPorId(1L);

        assertThat(encontrada.getNome()).isEqualTo("Maria Silva");
    }

    @Test
    void deveLancarExcecaoAoBuscarIdInexistente() {
        when(pessoaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().buscarPorId(99L))
                .isInstanceOf(PessoaNaoEncontradaException.class);
    }

    @Test
    void deveRemoverPessoaExistenteERetreinarModelo() {
        when(pessoaRepository.existsById(1L)).thenReturn(true);
        lenient().doNothing().when(modeloFacialService).retreinar();

        service().remover(1L);

        verify(pessoaRepository).deleteById(1L);
        verify(modeloFacialService, times(1)).retreinar();
    }

    @Test
    void deveLancarExcecaoAoRemoverIdInexistente() {
        when(pessoaRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service().remover(99L))
                .isInstanceOf(PessoaNaoEncontradaException.class);
        verify(pessoaRepository, never()).deleteById(any());
        verify(modeloFacialService, never()).retreinar();
    }

    private byte[] bytesRecurso(String nomeRecurso) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(nomeRecurso)) {
            return Objects.requireNonNull(in, "Recurso de teste não encontrado: " + nomeRecurso).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

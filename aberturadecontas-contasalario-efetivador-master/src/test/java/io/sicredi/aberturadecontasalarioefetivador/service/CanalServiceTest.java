package io.sicredi.aberturadecontasalarioefetivador.service;

import io.sicredi.aberturadecontasalarioefetivador.entities.Canal;
import io.sicredi.aberturadecontasalarioefetivador.factories.CanalFactory;
import io.sicredi.aberturadecontasalarioefetivador.repository.CanalRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CanalServiceTest {

    @Mock
    private CanalRepository canalRepository;
    @Mock
    private Random random;
    @InjectMocks
    private CanalService canalService;

    @Test
    @DisplayName("Deve criar canal com código auto gerado")
    void deveCriarCanalComCodigoAutoGerado() {
        var canal = CanalFactory.canalValido();
        canal.setId(null);
        var codigo = 1234L;
        
        when(canalRepository.existsByNome(canal.getNome())).thenReturn(false);
        when(this.random.nextInt(8889)).thenReturn((int)(codigo - 1111));
        when(canalRepository.existsByCodigo(codigo)).thenReturn(true).thenReturn(false);
        when(canalRepository.save(any(Canal.class))).thenAnswer(invocation -> {
            Canal c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        var novoCanal = canalService.criarCanal(Canal.builder().nome(canal.getNome()).ativo(true).build());

        assertCanalCriadoEAtivo(novoCanal, canal);

        verify(canalRepository, times(1)).existsByNome(canal.getNome());
        verify(canalRepository, times(2)).existsByCodigo(canal.getCodigo());
        verify(canalRepository, times(1)).save(any(Canal.class));
    }

    @Test
    @DisplayName("Deve criar canal com código informado")
    void deveCriarCanalComCodigoInformado() {
        var canal = CanalFactory.canalValido();
        canal.setId(null);
        
        when(canalRepository.existsByNome(canal.getNome())).thenReturn(false);
        when(canalRepository.save(any(Canal.class))).thenAnswer(invocation -> {
            Canal c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        var novoCanal = canalService.criarCanal(Canal.builder().nome(canal.getNome()).codigo(1234L).ativo(true).build());

        assertCanalCriadoEAtivo(novoCanal, canal);

        verify(canalRepository, times(1)).existsByNome(canal.getNome());
        verify(canalRepository, times(0)).existsByCodigo(canal.getCodigo());
        verify(canalRepository, times(1)).save(any(Canal.class));
    }

    @Test
    @DisplayName("Não deve criar canal com nome já existente")
    void naoDeveCriarCanalComNomeJaExistente() {
        var canal =  CanalFactory.canalValido();

        when(canalRepository.existsByNome(canal.getNome())).thenReturn(true);

        assertThatThrownBy(() -> canalService.criarCanal(canal))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Nome do canal já existe.");

        verify(canalRepository, times(1)).existsByNome(canal.getNome());
        verifyNoMoreInteractions(canalRepository);
    }

    @Test
    @DisplayName("Deve desativar canal")
    void deveDesativarCanal() {
        var canal =  CanalFactory.canalValido();

        when(canalRepository.findByCodigo(canal.getCodigo())).thenReturn(Optional.of(canal));
        when(canalRepository.save(any(Canal.class))).thenReturn(canal);

        var canalDesativado = canalService.desativarCanal(canal.getCodigo());

        assertThat(canalDesativado.isAtivo()).isFalse();
        verify(canalRepository, times(1)).findByCodigo(canal.getCodigo());
        verify(canalRepository, times(1)).save(canal);
    }

    @Test
    @DisplayName("Deve lançar Exception ao desativar canal inexistente")
    void deveLancarExceptionAoDesativarCanalInexistente() {
        var codigoInexistente = 9999L;

        when(canalRepository.findByCodigo(codigoInexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> canalService.desativarCanal(codigoInexistente))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Canal não encontrado.");

        verify(canalRepository, times(1)).findByCodigo(codigoInexistente);
        verifyNoMoreInteractions(canalRepository);
    }

    @Test
    @DisplayName("Deve consultar canal")
    void deveConsultarCanal() {
        var canal =  CanalFactory.canalValido();

        when(canalRepository.findByCodigo(canal.getCodigo())).thenReturn(Optional.of(canal));

        var canalOptional = canalService.consultarCanal(canal.getCodigo());

        assertThat(canalOptional).isPresent().contains(canal);
        verify(canalRepository, times(1)).findByCodigo(canal.getCodigo());
    }

    @Test
    @DisplayName("Deve consultar canal ativo")
    void deveConsultarCanalAtivo() {
        var canal =  CanalFactory.canalValido();

        when(canalRepository.findByCodigoAndAtivoTrue(canal.getCodigo())).thenReturn(Optional.of(canal));

        var canalOptional = canalService.consultarCanalAtivo(canal.getCodigo());

        assertThat(canalOptional).isPresent().contains(canal);
        verify(canalRepository, times(1)).findByCodigoAndAtivoTrue(canal.getCodigo());
    }

    @Test
    @DisplayName("Não deve consultar canal ativo quando o encontrado na base estiver inativo")
    void naoDeveConsultarCanalAtivoQuandoInativo() {
        var canal =  CanalFactory.canalInativo();

        when(canalRepository.findByCodigoAndAtivoTrue(canal.getCodigo())).thenReturn(Optional.empty());

        var canalOptional = canalService.consultarCanalAtivo(canal.getCodigo());

        assertThat(canalOptional).isEmpty();
        verify(canalRepository, times(1)).findByCodigoAndAtivoTrue(canal.getCodigo());
    }

    @Test
    @DisplayName("Deve consultar canal ativo pelo código e documento")
    void deveConsultarCanalAtivoPeloCodigoEDocumento() {
        var canal =  CanalFactory.canalValido();

        when(canalRepository.findByCodigoAndDocumentoAndAtivoIsTrue(canal.getCodigo(), canal.getDocumento())).thenReturn(Optional.of(canal));

        var canalOptional = canalService.consultarCanalAtivoPorCodigoEDocumento(canal.getCodigo(), canal.getDocumento());

        assertThat(canalOptional).isNotEmpty().contains(canal);
        verify(canalRepository, times(1)).findByCodigoAndDocumentoAndAtivoIsTrue(canal.getCodigo(), canal.getDocumento());
    }

    private static void assertCanalCriadoEAtivo(Canal novoCanal, Canal canal) {
        assertThat(novoCanal).isNotNull();
        assertThat(novoCanal.getCodigo()).isEqualTo(canal.getCodigo());
        assertThat(novoCanal.getNome()).isEqualTo(canal.getNome());
        assertThat(novoCanal.isAtivo()).isTrue();
    }
}
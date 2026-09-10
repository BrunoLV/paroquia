package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerenciarCicloVidaFielUseCaseTest {

    @Mock private FielJpaRepository fielRepository;
    @Mock private UsuarioJpaRepository usuarioRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);

    private GerenciarCicloVidaFielUseCase useCase() {
        return new GerenciarCicloVidaFielUseCase(fielRepository, usuarioRepository, clock);
    }

    private FielEntity fielComPii() {
        FielEntity f = new FielEntity();
        f.setId(UUID.randomUUID());
        f.setNome("Maria da Silva");
        f.setCpf("529.982.247-25");
        f.setEmail("maria@example.com");
        f.setCelular("11999998888");
        f.setEnderecoLogradouro("Rua A");
        f.setAtivo(false);
        return f;
    }

    @Test
    void anonimizarEliminaPiiEMarcaFlag() {
        FielEntity fiel = fielComPii();
        when(fielRepository.findByAtivoFalseAndDadosCadastraisEliminadosFalseAndDesativadoEmUtcBefore(any()))
                .thenReturn(List.of(fiel));

        useCase().anonimizarCadastrosVencidos();

        assertThat(fiel.getNome()).isNull();
        assertThat(fiel.getCpf()).isNull();
        assertThat(fiel.getCpfHash()).isNull();
        assertThat(fiel.getEmail()).isNull();
        assertThat(fiel.getEnderecoLogradouro()).isNull();
        assertThat(fiel.isDadosCadastraisEliminados()).isTrue();
    }

    @Test
    void reativarRejeitaCadastroAnonimizado() {
        FielEntity fiel = fielComPii();
        fiel.anonimizar(Instant.now(clock), "sistema");
        UUID id = fiel.getId();
        when(fielRepository.findById(id)).thenReturn(Optional.of(fiel));

        assertThatThrownBy(() -> useCase().reativar(id, "admin"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void desativarMarcaInativoEDesativadoEm() {
        FielEntity fiel = fielComPii();
        fiel.setAtivo(true);
        UUID id = fiel.getId();
        when(fielRepository.findById(id)).thenReturn(Optional.of(fiel));

        useCase().desativar(id, "admin");

        assertThat(fiel.isAtivo()).isFalse();
        assertThat(fiel.getDesativadoEmUtc()).isEqualTo(Instant.now(clock));
    }
}

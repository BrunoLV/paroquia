package br.com.nsfatima.gestao.fiel.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.RelacionamentoFamiliarEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielDependenteJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.RelacionamentoFamiliarJpaRepository;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.HistoricoConsentimentoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GerenciarCicloVidaFielUseCaseTest {

    @Mock private FielJpaRepository fielRepository;
    @Mock private UsuarioJpaRepository usuarioRepository;
    @Mock private FielDependenteJpaRepository dependenteRepository;
    @Mock private RelacionamentoFamiliarJpaRepository relacionamentoRepository;
    @Mock private HistoricoConsentimentoJpaRepository historicoRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);

    private GerenciarCicloVidaFielUseCase useCase() {
        return new GerenciarCicloVidaFielUseCase(
                fielRepository, usuarioRepository, dependenteRepository, relacionamentoRepository,
                historicoRepository, clock);
    }

    private FielEntity fielComPii() {
        FielEntity f = new FielEntity();
        f.setId(UUID.randomUUID());
        f.setNome("Maria da Silva");
        f.setCpf("529.982.247-25");
        f.setEmail("maria@example.com");
        f.setCelular("11999998888");
        f.setEnderecoLogradouro("Rua A");
        f.setDataNascimento(LocalDate.of(1980, 3, 2));
        f.setSexo(Sexo.FEMININO);
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
        assertThat(fiel.getDataNascimento()).isNull();
        assertThat(fiel.getSexo()).isNull();
    }

    @Test
    void anonimizarEliminaDependentesRelacoesTrilhaEConta() {
        FielEntity fiel = fielComPii();
        UsuarioEntity conta = new UsuarioEntity();
        conta.setId(UUID.randomUUID());
        conta.setUsername("maria.silva");
        conta.setEnabled(true);
        RelacionamentoFamiliarEntity relacao = new RelacionamentoFamiliarEntity();
        when(fielRepository.findByAtivoFalseAndDadosCadastraisEliminadosFalseAndDesativadoEmUtcBefore(any()))
                .thenReturn(List.of(fiel));
        when(relacionamentoRepository.findVigentesDaPessoa(fiel.getId())).thenReturn(List.of(relacao));
        when(usuarioRepository.findByFielId(fiel.getId())).thenReturn(Optional.of(conta));

        useCase().anonimizarCadastrosVencidos();

        verify(dependenteRepository).excluirPorFiel(fiel.getId());
        verify(historicoRepository).anonimizarDadosPessoais("FIEL", fiel.getId());
        assertThat(relacao.getDeletedAt()).isEqualTo(Instant.now(clock));
        assertThat(conta.isEnabled()).isFalse();
        assertThat(conta.getUsername()).doesNotContain("maria");
        verify(usuarioRepository).save(conta);
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

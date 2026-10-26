package br.com.nsfatima.gestao.clero.application.usecase;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.RelacionamentoFamiliarEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.RelacionamentoFamiliarJpaRepository;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.HistoricoConsentimentoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Anonimizacao do clerigo em paridade com o fiel")
class GerenciarCicloVidaClerigoUseCaseTest {

    @Mock private ClerigoJpaRepository clerigoRepository;
    @Mock private UsuarioJpaRepository usuarioRepository;
    @Mock private RelacionamentoFamiliarJpaRepository relacionamentoRepository;
    @Mock private HistoricoConsentimentoJpaRepository historicoRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);

    private ClerigoEntity clerigoComPii() {
        ClerigoEntity c = new ClerigoEntity();
        c.setId(UUID.randomUUID());
        c.setNomeCivil("José da Silva");
        c.setNomeEclesiastico("Pe. José");
        c.setDataNascimento(LocalDate.of(1970, 1, 2));
        c.setCpf("529.982.247-25");
        c.setEmail("jose@exemplo.com");
        c.setCelular("11999998888");
        c.setEnderecoLogradouro("Rua A");
        c.setTermoConsentimentoAceito(true);
        c.setIpConsentimento("10.0.0.1");
        c.setAtivo(false);
        return c;
    }

    @Test
    @DisplayName("zera dados civis, blind index e consentimento; preserva o nome eclesiastico canonico")
    void anonimizaDadosCivisEPreservaDadosCanonicos() {
        ClerigoEntity clerigo = clerigoComPii();
        assertThat(clerigo.getCpfHash()).hasSize(64);
        UsuarioEntity conta = new UsuarioEntity();
        conta.setId(UUID.randomUUID());
        conta.setUsername("pe.jose");
        RelacionamentoFamiliarEntity relacao = new RelacionamentoFamiliarEntity();
        when(clerigoRepository.findByAtivoFalseAndDadosCadastraisEliminadosFalseAndDeletedAtBefore(any()))
                .thenReturn(List.of(clerigo));
        when(relacionamentoRepository.findVigentesDaPessoa(clerigo.getId())).thenReturn(List.of(relacao));
        when(usuarioRepository.findByClerigoId(clerigo.getId())).thenReturn(Optional.of(conta));

        new GerenciarCicloVidaClerigoUseCase(
                clerigoRepository, usuarioRepository, relacionamentoRepository, historicoRepository, clock)
                .anonimizarCadastrosVencidos();

        assertThat(clerigo.getNomeCivil()).isNull();
        assertThat(clerigo.getCpf()).isNull();
        assertThat(clerigo.getCpfHash()).isNull();
        assertThat(clerigo.getEmail()).isNull();
        assertThat(clerigo.getCelular()).isNull();
        assertThat(clerigo.getDataNascimento()).isNull();
        assertThat(clerigo.getEnderecoLogradouro()).isNull();
        assertThat(clerigo.getIpConsentimento()).isNull();
        assertThat(clerigo.isTermoConsentimentoAceito()).isFalse();
        assertThat(clerigo.isDadosCadastraisEliminados()).isTrue();
        assertThat(clerigo.getNomeEclesiastico()).isEqualTo("Pe. José");
        assertThat(relacao.getDeletedAt()).isEqualTo(Instant.now(clock));
        assertThat(conta.isEnabled()).isFalse();
        assertThat(conta.getUsername()).doesNotContain("jose");
        verify(historicoRepository).anonimizarDadosPessoais("CLERIGO", clerigo.getId());
        verify(clerigoRepository).save(clerigo);
    }

    @Test
    @DisplayName("blind index do CPF e recalculado a cada troca de CPF")
    void blindIndexAcompanhaOCpf() {
        ClerigoEntity c = new ClerigoEntity();
        c.setCpf("529.982.247-25");
        String primeiro = c.getCpfHash();
        c.setCpf("111.444.777-35");

        assertThat(c.getCpfHash()).isNotEqualTo(primeiro).hasSize(64);
    }
}

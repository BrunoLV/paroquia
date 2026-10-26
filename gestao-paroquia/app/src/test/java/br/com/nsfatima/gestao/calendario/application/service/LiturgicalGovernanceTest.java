package br.com.nsfatima.gestao.calendario.application.service;

import java.time.ZoneId;
import java.time.Clock;
import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.calendario.domain.exception.RegraEclesialImutavelException;
import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.AnoLiturgicoRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.CicloLiturgicoRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.SolenidadeLiturgicaJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Cobre apenas as regras puras (sem I/O) do serviço: pesos de precedência e direitos de
 * mutação manual. Os repositórios são mocks porque o construtor exige colaboradores não-nulos,
 * mas nenhum deles é exercitado pelos métodos testados aqui.
 */
class LiturgicalGovernanceTest {

    private static final RelogioNegocio RELOGIO = new RelogioNegocio(
            Clock.systemUTC(), ZoneId.of("America/Sao_Paulo"));

    private final ProvisionamentoLiturgicoService service = new ProvisionamentoLiturgicoService(
            mock(AnoLiturgicoRepository.class),
            mock(CicloLiturgicoRepository.class),
            new MotorLiturgico(RELOGIO),
            mock(EventoJpaRepository.class),
            mock(UnidadeTerritorialJpaRepository.class),
            mock(SolenidadeLiturgicaJpaRepository.class));

    @Test
    @DisplayName("Deve permitir edição manual apenas para perfis PAROCO e VIGARIO")
    void devePermitirEdicaoManualApenasParaParocoEVigario() {
        // Permitido para PAROCO e VIGARIO
        service.validateManualMutationRights("PAROCO", "corLiturgica", "ROXO", "Ajuste pastoral");
        service.validateManualMutationRights("VIGARIO", "corLiturgica", "ROXO", "Ajuste pastoral");

        // Bloqueado para outros perfis
        assertThatThrownBy(() -> service.validateManualMutationRights("SECRETARIA", "corLiturgica", "ROXO", "Ajuste"))
                .isInstanceOf(OperacaoProibidaException.class)
                .hasMessageContaining("Apenas Pároco ou Vigário podem alterar cadastros litúrgicos");

        assertThatThrownBy(() -> service.validateManualMutationRights("COORDENADOR", "corLiturgica", "ROXO", "Ajuste"))
                .isInstanceOf(OperacaoProibidaException.class);
    }

    @Test
    @DisplayName("Deve disparar RegraEclesialImutavelException ao tentar alterar cicloLetra ou data da Páscoa")
    void deveDispararExcecaoParaCamposImutaveis() {
        assertThatThrownBy(() -> service.validateManualMutationRights("PAROCO", "cicloLetra", "A",
                "Tentativa de alteração"))
                .isInstanceOf(RegraEclesialImutavelException.class)
                .hasMessageContaining("regras eclesiais imutáveis");

        assertThatThrownBy(() -> service.validateManualMutationRights("PAROCO", "PASCOA", "2026-04-10",
                "Tentativa de alteração"))
                .isInstanceOf(RegraEclesialImutavelException.class)
                .hasMessageContaining("regras eclesiais imutáveis");
    }

    @Test
    @DisplayName("Deve respeitar a hierarquia de pesos de precedência litúrgica (1 a 5)")
    void deveRespeitarPesosPrecedenciaLiturgica() {
        assertThat(service.resolvePrecedenciaPeso(TipoCicloLiturgico.TRIDUO_PASCAL)).isEqualTo(1);
        assertThat(service.resolvePrecedenciaPeso(TipoCicloLiturgico.SOLENIDADE)).isEqualTo(2);
        assertThat(service.resolvePrecedenciaPeso(TipoCicloLiturgico.QUARESMA)).isEqualTo(3);
        assertThat(service.resolvePrecedenciaPeso(TipoCicloLiturgico.ADVENTO)).isEqualTo(3);
        assertThat(service.resolvePrecedenciaPeso(TipoCicloLiturgico.PASCOA)).isEqualTo(4);
        assertThat(service.resolvePrecedenciaPeso(TipoCicloLiturgico.NATAL)).isEqualTo(4);
        assertThat(service.resolvePrecedenciaPeso(TipoCicloLiturgico.TEMPO_COMUM)).isEqualTo(5);
    }
}

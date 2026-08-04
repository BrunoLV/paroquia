package br.com.nsfatima.gestao.calendario.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.domain.exception.ImmutableEclesialRuleException;
import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LiturgicalGovernanceTest {

    private final ProvisionamentoLiturgicoService service = new ProvisionamentoLiturgicoService(null, null,
            new MotorLiturgico());

    @Test
    @DisplayName("Deve permitir edição manual apenas para perfis PAROCO e VIGARIO")
    void devePermitirEdicaoManualApenasParaParocoEVigario() {
        // Permitido para PAROCO e VIGARIO
        service.validateManualMutationRights("PAROCO", "corLiturgica", "ROXO", "Ajuste pastoral");
        service.validateManualMutationRights("VIGARIO", "corLiturgica", "ROXO", "Ajuste pastoral");

        // Bloqueado para outros perfis
        assertThatThrownBy(() -> service.validateManualMutationRights("SECRETARIA", "corLiturgica", "ROXO", "Ajuste"))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("Apenas Pároco ou Vigário podem alterar cadastros litúrgicos");

        assertThatThrownBy(() -> service.validateManualMutationRights("COORDENADOR", "corLiturgica", "ROXO", "Ajuste"))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    @DisplayName("Deve disparar ImmutableEclesialRuleException ao tentar alterar cicloLetra ou data da Páscoa")
    void deveDispararExcecaoParaCamposImutaveis() {
        assertThatThrownBy(() -> service.validateManualMutationRights("PAROCO", "cicloLetra", "A",
                "Tentativa de alteração"))
                .isInstanceOf(ImmutableEclesialRuleException.class)
                .hasMessageContaining("regras eclesiais imutáveis");

        assertThatThrownBy(() -> service.validateManualMutationRights("PAROCO", "PASCOA", "2026-04-10",
                "Tentativa de alteração"))
                .isInstanceOf(ImmutableEclesialRuleException.class)
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
